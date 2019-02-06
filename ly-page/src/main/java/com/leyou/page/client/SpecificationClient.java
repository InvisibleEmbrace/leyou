package com.leyou.page.client;

import com.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2019/1/23
 */
@FeignClient(value = "item-service")
public interface SpecificationClient extends SpecificationApi {

}