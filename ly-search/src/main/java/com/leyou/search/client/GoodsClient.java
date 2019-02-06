package com.leyou.search.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/12/10
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {

}
