package com.leyou.service.impl;


import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.api.BrandApi;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.Sku;
import com.leyou.pojo.Goods;
import com.leyou.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/12/10
 */
public class SearchServiceImpl implements SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandApi brandApi;

    @Autowired
    private GoodsClient goodsClient;

    @Override
    public Goods buildGoods(SpuBo spuBo) {
        Goods goods = new Goods();
        // 查询分类名称
        List<String> CategoryNameList = categoryClient.queryByIds(Arrays.asList(spuBo.getCid1(), spuBo.getCid2(), spuBo.getCid3()))
                .stream()
                .map(Category::getName)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(CategoryNameList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        // 查询品牌
        Brand brand = brandApi.queryById(spuBo.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        // 所有的搜索字段拼接到all中，all存入索引库，并进行分词处理，搜索时与all中的字段进行匹配查询
        String all = spuBo.getTitle() + StringUtils.join(CategoryNameList, " ") + brand.getName();

        // 查询sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spuBo.getId());
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

        return null;
    }
}
