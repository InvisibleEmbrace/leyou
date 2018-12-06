package com.leyou.item.mapper;

import com.leyou.item.pojo.Stock;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;


/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/11/29
 */
public interface StockMapper extends Mapper<Stock>, IdListMapper<Stock, Long> {

}
