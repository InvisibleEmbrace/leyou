package com.leyou.service;


import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.pojo.Goods;
import com.leyou.pojo.SearchRequest;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/12/6
 */
public interface SearchService {

    Goods buildGoods(Spu spu);

    PageResult<Goods> search(SearchRequest request);

}
