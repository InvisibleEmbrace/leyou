package com.leyou.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecificationClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.util.JsonUtils;
import com.leyou.common.util.NumberUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.api.BrandApi;
import com.leyou.item.pojo.*;
import com.leyou.pojo.Goods;
import com.leyou.pojo.SearchRequest;
import com.leyou.repository.GoodsRepository;
import com.leyou.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandApi brandApi;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository repository;

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
        Brand brand = brandApi.queryById(spu.getBrandId());
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
        List<SpecParam> params = specificationClient.querySpecParam(null, spu.getCid3(), null, true);
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
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", key));
        // 2.3、分页
        queryBuilder.withPageable(PageRequest.of(page, size));

        // 3、返回结果
        Page<Goods> result = this.repository.search(queryBuilder.build());

        // 4、解析结果
        long total = result.getTotalElements();
        long totalPage = (total + size - 1) / size;
        return new PageResult<>(total, totalPage, result.getContent());
    }
}
