package com.leyou.item.service;

import com.leyou.item.pojo.Category;

import java.util.List;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/11/16
 */
public interface CategoryService {

    List<Category> queryCategoryByPid(Long pid);

    List<Category> queryCategoryByIds(List<Long> ids);

    List<Category> queryAllByCid3(Long id);

    List<Category> queryByBrandId(Long id);

    // 根据分类id查询分类名
    List<String> queryNameByIds(List<Long> idList);
}
