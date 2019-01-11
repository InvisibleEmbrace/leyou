package com.leyou.item.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/12/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuVo {

    private Long id;
    private String title;
    private Long price;
    private String image;
}
