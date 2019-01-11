package com.leyou.item.web;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/11/26
 */
@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询SPU
     *
     * @param page
     * @param rows
     * @param key
     * @return
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key) {
        // 分页查询spu信息
        PageResult<Spu> result = this.goodsService.querySpuByPageAndSort(page, rows, saleable, key);
        if (result == null || result.getItems().size() == 0) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 新增商品
     *
     * @param
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu) {
        try {
            this.goodsService.save(spu);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 根据spuId查询sku库存列表
     * @param id
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id") Long id) {
        List<Sku> skus = this.goodsService.querySkuBySpuId(id);
        if (skus == null || skus.size() == 0) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return ResponseEntity.ok(skus);
    }


    /**
     * 新增商品
     *
     * @param spu
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu) {
        try {
            this.goodsService.update(spu);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
    }


    /**
     * 查询spu详情
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId") Long spuId) {
        return ResponseEntity.ok(goodsService.querySpuDetailBySpuId(spuId));
    }


}