package com.leyou.page.web;

import com.leyou.page.service.FileService;
import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2019/1/23
 *
 */
@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @Autowired
    private FileService fileService;

    /**
     * 跳转到商品详情页
     * @param model
     * @param id
     * @return
     */
    @GetMapping("item/{id}.html")
    public String toItemPage(Model model, @PathVariable("id")Long id){
        // 加载所需的数据
        Map<String, Object> modelMap;
        modelMap = this.pageService.loadModel(id);
        // 判断是否需要生成新的页面
        if(!this.fileService.exists(id)){
            this.fileService.syncCreateHtml(id);
        }
        // 放入模型
        model.addAllAttributes(modelMap);
        return "item";
    }
}
