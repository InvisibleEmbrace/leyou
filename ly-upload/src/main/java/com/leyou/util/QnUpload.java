package com.leyou.util;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.web.multipart.MultipartFile;


/**
 * @Author: fjw◕‿◕
 * @Description: 七牛图片上传
 * @Date: 2018/11/27
 */
public class QnUpload {

    private String baseUrl = "http://piufne90u.bkt.clouddn.com";

    public String uploadImage(MultipartFile file) {
        System.out.println("进入上传方法");
        //构造一个带指定Zone对象的配置类，Zone.zone2()为华南地区
        Configuration cfg = new Configuration(Zone.zone2());
//...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
//...生成上传凭证，然后准备上传
        String accessKey = "hi3sEb4tF1YAexKcv7jvpOMsThhvkEuJKT1l9DJE";
        String secretKey = "vjKjoin2KOYupxI-PnNJTYcHyIb1LMPgdkiJJTGx";
        String bucket = "leyou";
//默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = null;
        DefaultPutRet putRet = null;
        try {
            byte[] uploadBytes = file.getBytes();
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);
            try {
                Response response = uploadManager.put(uploadBytes, key, upToken);
                //解析上传成功的结果
               putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    ex2.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return baseUrl + "/" + putRet.key;
    }
}
