package com.leyou.search.service;


import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/12/6
 */
public interface SearchService {

    Goods buildGoods(Spu spu);

    PageResult<Goods> search(SearchRequest request);

    void createOrUpateIndex(Long spuId);

    void deleteById(Long spuId);
}
