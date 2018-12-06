package com.leyou.repository;

import com.leyou.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/12/5
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {

}
