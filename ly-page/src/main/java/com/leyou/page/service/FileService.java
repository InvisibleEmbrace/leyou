package com.leyou.page.service;

import com.leyou.common.util.ThreadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;

@Service
public class FileService {

    @Autowired
    private PageService pageService;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${ly.thymeleaf.destPath}")
    private String destPath;// D:/heima/nginx-1.12.2/html

    public void createHtml(Long id) throws Exception {
        // 创建thymeleaf上下文
        Context context = new Context();
        // 把数据放入上下文
        context.setVariables(pageService.loadModel(id));
        // 创建输出流关联一个临时文件
        File temp = new File(id + ".html");
        File dest = createPath(id);
        // 备份原文件（重命名）
        File bak = new File(id + "_bak.html");
        try (PrintWriter writer = new PrintWriter(temp, "UTF-8")) {
            // 利用模板引擎生成页面
            templateEngine.process("item", context, writer);
            if (dest.exists()) {
                // 如果目标文件已经存在，先备份
                dest.renameTo(bak);
            }
            // 将新页面覆盖旧页面
            FileCopyUtils.copy(temp, dest);
            // 成功后将备份页面删除
            bak.delete();
        } catch (IOException e) {
            // 失败后，将备份页面恢复
            bak.renameTo(dest);
            // 重新抛出异常，声明页面生成失败
            throw new Exception(e);
        } finally {
            // 删除临时页面
            if (temp.exists()) {
                temp.delete();
            }
        }
    }

    /**
     * 创建路径
     * @param id
     * @return
     */
    public File createPath(Long id) {
        if (id == null) {
            return null;
        }
        File dest = new File(destPath);
        if (!dest.exists()) {
            dest.mkdirs();
        }
        return new File(dest, id + ".html");
    }

    /**
     * 判断某个商品的页面是否存在
     * @param id
     * @return
     */
    public boolean exists(Long id){
        return this.createPath(id).exists();
    }

    /**
     * 异步创建html页面
     * @param id
     */
    public void syncCreateHtml(Long id){
        ThreadUtils.execute(()->{
            try{
                createHtml(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void deleteHtml(Long id) {
        File file = new File(this.destPath, id + ".html");
        file.deleteOnExit();
    }
}
