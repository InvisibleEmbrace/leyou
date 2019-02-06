package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/12/5
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {

}
