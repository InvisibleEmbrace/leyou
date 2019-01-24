package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;

import java.util.List;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/11/29
 */
public interface GoodsService {

    PageResult<Spu> querySpuByPageAndSort(Integer page, Integer rows, Boolean saleable, String key);

    SpuDetail querySpuDetailById(Long id);

    List<Sku> querySkuBySpuId(Long spuId);

    void save(Spu spu);

    void update(Spu spu);

    SpuDetail querySpuDetailBySpuId(Long spuId);

    Spu querySpuById(Long id);


}
