package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpuDetail;

import java.util.List;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/11/29
 */
public interface GoodsService {

    PageResult<SpuBo> querySpuByPageAndSort(Integer page, Integer rows, Boolean saleable, String key);

    void save(SpuBo spuBo);

    SpuDetail querySpuDetailById(Long id);

    List<Sku> querySkuBySpuId(Long spuId);

    void update(SpuBo spu);
}
