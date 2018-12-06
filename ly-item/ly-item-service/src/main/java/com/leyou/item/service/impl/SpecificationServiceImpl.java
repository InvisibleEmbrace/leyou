package com.leyou.item.service.impl;

import com.leyou.item.mapper.SpecificationMapper;
import com.leyou.item.pojo.Specification;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/11/28
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;

    @Override
    public Specification queryById(Long id) {
        return specificationMapper.selectByPrimaryKey(id);
    }
}
