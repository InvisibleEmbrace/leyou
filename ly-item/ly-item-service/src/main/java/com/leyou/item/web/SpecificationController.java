package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: fjw◕‿◕
 * @Description: 规格参数
 * @Date: 2018/11/28
 */
@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 查询规格
     * @param gid
     * @return
     */
    @GetMapping("/params")
    public ResponseEntity<List<SpecParam>> querySpecParam( @RequestParam(value="gid", required = false) Long gid) {
        List<SpecParam> list = specificationService.querySpecParams(gid);
        return ResponseEntity.ok(list);
    }


    /**
     * 查询规格组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroups(@PathVariable("cid") Long cid) {
        List<SpecGroup> specGroups = specificationService.queryGroupByCid(cid);
        return ResponseEntity.ok(specGroups);
    }

}
