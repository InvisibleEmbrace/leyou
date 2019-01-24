package com.leyou.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecificationClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.util.JsonUtils;
import com.leyou.common.util.NumberUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.pojo.Goods;
import com.leyou.pojo.SearchRequest;
import com.leyou.pojo.SearchResult;
import com.leyou.repository.GoodsRepository;
import com.leyou.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.UnmappedTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/12/10
 */
@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository repository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public Goods buildGoods(Spu spu) {
        // 查询分类名称
        List<String> CategoryNameList = categoryClient.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                .stream()
                .map(Category::getName)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(CategoryNameList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        // 查询品牌
        Brand brand = brandClient.queryById(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        // 所有的搜索字段拼接到all中，all存入索引库，并进行分词处理，搜索时与all中的字段进行匹配查询
        String all = spu.getTitle() + StringUtils.join(CategoryNameList, " ") + brand.getName();

        // 查询sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spu.getId());
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //存储price的集合
        TreeSet<Long> priceSet = new TreeSet<>();

        //设置存储skus的json结构的集合，用map结果转化sku对象，转化为json之后与对象结构相似（或者重新定义一个对象，存储前台要展示的数据，并把sku对象转化成自己定义的对象）
        List<Map<String, Object>> skus = new ArrayList<>();
        //从sku中取出要进行展示的字段，并将sku转换成json格式
        skuList.forEach(sku -> {
            priceSet.add(sku.getPrice());
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            //sku中有多个图片，只展示第一张
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            map.put("price", sku.getPrice());
            skus.add(map);
        });
        // 查询规格参数，规格参数中分为通用规格参数和特有规格参数
        // 查询规格参数
        List<SpecParam> params = specificationClient.querySpecParam(null, spu.getCid3(), null, null);
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spu.getId());
        // 处理规格参数
        Map<Long, String> genericMap = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
        Map<Long, List<String>> specialMap = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {});
        Map<String, Object> specs = new HashMap<>();
        for (SpecParam param : params) {
            if(param.getGeneric()){
                // 通用参数
                String value = genericMap.get(param.getId());
                if(param.getNumeric()){
                    // 数值类型，需要存储一个分段
                    value = this.chooseSegment(value, param);
                }
                specs.put(param.getName(), value);
            }else{
                // 特有参数
                specs.put(param.getName(), specialMap.get(param.getId()));
            }
        }
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        // 搜索条件 拼接：标题、分类、品牌
        goods.setAll(spu.getTitle() + " " + StringUtils.join(CategoryNameList, " "));
        goods.setPrice(priceSet);
        goods.setSkus(JsonUtils.serialize(skuList));
        goods.setSpecs(specs);

        return goods;
    }



    // 处理数值区间   规格字段
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 搜索
     * @param request
     * @return
     */
    @Override
    public PageResult<Goods> search(SearchRequest request) {
        String key = request.getKey();
        if(StringUtils.isBlank(key)){
            // 如果用户没搜索条件，我们可以给默认的，或者返回null
            return null;
        }
        Integer page = request.getPage() - 1;// elasticsearch的page 从0开始
        Integer size = request.getSize();
        // 1.创建查询构造器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 2.查询
        // 2.1、对结果进行筛选
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));
        // 2.2、基本查询
        QueryBuilder basicQuery = buildBasicQueryWithFilter(request);
        queryBuilder.withQuery(basicQuery);
        // 分页
        // 2.3分页排序
        searchWithPageAndSort(queryBuilder,request);

        // 3、聚合
        String categoryAggName = "category"; // 商品分类聚合名称
        String brandAggName = "brand"; // 品牌聚合名称
        // 对商品分类进行聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        // 3、返回结果
        AggregatedPage<Goods> pageInfo = (AggregatedPage<Goods>)this.repository.search(queryBuilder.build());
        // 解析聚合结果
        Aggregations aggs = pageInfo.getAggregations();
        List<Category> categories = getCategoryAggResult(aggs.get(categoryAggName));
        List<Brand> brands = getBrandAggResult(aggs.get(brandAggName));
        // 完成规格参数聚合
        // 判断商品分类数量，看是否需要对规格参数进行聚合
        List<Map<String, Object>> specs = null;
        if (categories != null && categories.size() == 1) {
            // 如果分类只剩下一个，才进行规格参数过滤
            specs = getSpecs(categories.get(0).getId(), basicQuery);
        }
        // 4、解析结果
        long total = pageInfo.getTotalElements();
        long totalPage = (total + size - 1) / size;
        return new SearchResult(total, totalPage, pageInfo.getContent(), categories, brands, specs);
    }


    // 解析品牌聚合结果
    private List<Brand> getBrandAggResult(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Brand> brands = brandClient.queryBrandsByIds(ids);
            return brands;
        } catch (Exception e) {
            log.error("[搜索服务]查询品牌异常", e);
            return null;
        }
    }


    // 解析商品分类聚合结果
    private List<Category> getCategoryAggResult(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(c -> c.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Category> categories = categoryClient.queryByIds(ids);
            return categories;
        } catch (Exception e) {
            log.error("[搜索服务]查询分类异常", e);
            return null;
        }
    }

    // 构建基本查询条件
    private void searchWithPageAndSort(NativeSearchQueryBuilder queryBuilder, SearchRequest request) {
        // 准备分页参数
        int page = request.getPage();
        int size = request.getSize();

        // 1、分页
        queryBuilder.withPageable(PageRequest.of(page - 1, size));
        // 2、排序
        String sortBy = request.getSortBy();
        boolean desc = request.isDescending();
        if (StringUtils.isNotBlank(sortBy)) {
            // 如果不为空，则进行排序
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc ? SortOrder.DESC : SortOrder.ASC));
        }
    }

    // 聚合规格参数
    private List<Map<String, Object>> getSpecs(Long cid, QueryBuilder query) {
        // 根据分类查询规格
        List<SpecParam> params = this.specificationClient.querySpecParam(null, cid, true, null);
        // 创建集合保存过滤条件
        List<Map<String, Object>> specs = new ArrayList<>();
         // 聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 带上条件
        queryBuilder.withQuery(query);
        // 聚合规格参数
        params.stream().map(SpecParam::getName).map(key -> AggregationBuilders.terms(key).field("specs." + key + ".keyword")).forEach(queryBuilder::addAggregation);
        // 获取结果
        AggregatedPage<Goods> aggregatedPage = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);
        // 解析结果
        Aggregations aggregations = aggregatedPage.getAggregations();
        for (SpecParam param : params) {
            String name = param.getName();
            if (aggregations.get(name) instanceof UnmappedTerms) {
                continue;
            }
            StringTerms terms =  aggregations.get(name);
            // 准备Map
            Map<String, Object> map = new HashMap<>();
            map.put("k", name);
            map.put("options", terms.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList()));
            specs.add(map);
        }
        return specs;
    }


    // 过滤查询
    public QueryBuilder buildBasicQueryWithFilter(SearchRequest request) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 基本查询
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
        // 过滤条件构造器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        // 整理过滤条件
        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 商品分类和品牌要特殊处理
            if (key != "cid3" && key != "brandId") {
                key = "specs." + key + ".keyword";
            }
            // 字符串类型，进行term查询
            filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
        }
        // 添加过滤条件
        queryBuilder.filter(filterQueryBuilder);
        return queryBuilder;
    }
}
