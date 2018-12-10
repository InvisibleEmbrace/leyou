package com.leyou.item.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/11/28
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecParamMapper specParamMapper;


    @Override
    public List<SpecParam> querySpecParams(Long gid) {
        SpecParam specParam = new SpecParam();
        specParam.setId(gid);
        List<SpecParam> list = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return list;
    }
}
