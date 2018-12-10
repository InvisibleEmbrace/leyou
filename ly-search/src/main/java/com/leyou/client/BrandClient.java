package com.leyou.client;

import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/12/10
 */
@FeignClient("item-service")
public interface BrandClient extends BrandApi {

}
