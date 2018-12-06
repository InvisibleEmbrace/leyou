package com.leyou.item.api;

import com.leyou.item.pojo.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/11/20
 */
@RequestMapping("category")
public interface CategoryApi {

    @GetMapping("names")
    List<String> queryNameByIds(@RequestParam("ids") List<Long> ids);


    @GetMapping("/list")
    List<Category> queryCategoryByPid(@RequestParam(value = "pid", defaultValue = "0") Long pid);

}
     