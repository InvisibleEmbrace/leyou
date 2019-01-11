package com.leyou.search.client;

import com.leyou.client.GoodsClient;
import com.leyou.client.SpecificationClient;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.pojo.Goods;
import com.leyou.repository.GoodsRepository;
import com.leyou.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: fjw◕‿◕
 * @Description: TODO
 * @Date: 2018/12/23
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodRepositoryTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private SearchService searchService;

    @Test
    public void loadData(){
        int page = 1;
        int rows = 100;
        int size = 0;
        do {
            // 查询spu
            PageResult<Spu> result = goodsClient.querySpuByPage(page, rows, true, null);
            List<Spu> spuList = result.getItems();
            if (CollectionUtils.isEmpty(spuList)) {
                break;
            }
            // spu转为goods
            List<Goods> goods = spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());

            // 把goods放入索引库
            this.goodsRepository.saveAll(goods);

            size = spuList.size();
            page++;
        }while (size == 100);
    }
}
