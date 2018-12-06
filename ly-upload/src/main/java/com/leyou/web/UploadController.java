package com.leyou.web;

import com.leyou.util.QnUpload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/11/27
 */
@RestController
@RequestMapping("upload")
public class UploadController {


    @PostMapping("/image")
    public String uploadImage(@RequestParam("file") MultipartFile file) {
        QnUpload qn = new QnUpload();
        return qn.uploadImage(file);
    }

    @GetMapping
    public void  test() {
        System.out.println("你好OOOOOOOOOOOOOOO");
    }

}
