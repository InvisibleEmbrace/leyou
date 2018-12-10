package com.leyou.item.web;

import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/params")
    public ResponseEntity<List<SpecParam>> querySpecParam( @RequestParam(value="gid", required = false) Long gid) {
        List<SpecParam> list = specificationService.querySpecParams(gid);
        return ResponseEntity.ok(list);
    }
}
