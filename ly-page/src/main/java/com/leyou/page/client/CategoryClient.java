package com.leyou.page.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2019/1/23
 */
@FeignClient("item-service")
public interface CategoryClient extends CategoryApi {

}