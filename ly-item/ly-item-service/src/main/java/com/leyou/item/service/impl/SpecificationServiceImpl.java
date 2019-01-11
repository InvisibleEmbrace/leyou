package com.leyou.item.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private SpecGroupMapper specGroupMapper;



    @Override
    public List<SpecParam> querySpecParams(Long gid, Long cid, Boolean searching, Boolean generic) {
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setSearching(searching);
        param.setGeneric(generic);
        return this.specParamMapper.select(param);
    }

    @Override
    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> result = specGroupMapper.select(specGroup);
        if (result == null) {
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return result;
    }
}
