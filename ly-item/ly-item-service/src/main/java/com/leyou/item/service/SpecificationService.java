package com.leyou.item.service;

import com.leyou.item.pojo.SpecParam;


import java.util.List;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/11/28
 */
public interface SpecificationService {

    List<SpecParam> querySpecParams(Long gid);

}
