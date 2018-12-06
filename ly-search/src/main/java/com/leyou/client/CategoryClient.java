package com.leyou.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/12/5
 */
@FeignClient("item-service")
public interface CategoryClient  extends CategoryApi {

}
