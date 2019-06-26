package com.liliang.gmall.service;


import com.liliang.gmall.bean.dto.SkuLsInfo;
import com.liliang.gmall.bean.dto.SkuLsParams;
import com.liliang.gmall.bean.dto.SkuLsResult;

/**
 *定义ListService接口,主要功能是商品上架到网站上，把数据放到全文检索elasticseacrh中，
 * 用户在首页的查询会从此接口到elasticseacrh中查询
 * @author liliang
 * @since
 */
public interface ListService {

    /**
     * 保存数据到elasticseacrh中
     * @param skuLsInfo
     */
    void saveSkuLsInfo(SkuLsInfo skuLsInfo);

    /**
     *
     * @param skuLsParams {用户在前台输入的参数}
     * @return
     */
    SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * 设置商品的热度，更新商品的热度
     * @param skuId
     */
    void incrHotScore(String skuId);
}
