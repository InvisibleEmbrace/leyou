package com.leyou.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/12/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    private Long skuId;  //商品skuId

    private Integer num;  //购买数量
}
