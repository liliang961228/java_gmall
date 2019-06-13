package com.liliang.gmall.service;

import com.liliang.gmall.bean.SkuImage;
import com.liliang.gmall.bean.SkuInfo;
import com.liliang.gmall.bean.SkuSaleAttrValue;
import com.liliang.gmall.bean.SpuSaleAttr;

import java.util.List;

/**
 * 商品详情查找
 * @author liliang
 * @since 2019.06.12
 */
public interface ItemService {

    /**
     * 通过skuId查询到指定的sku_Info的信息
     * @param skuId
     * @return SkuInfo
     */
    SkuInfo getSkuInfoBySkuId(String skuId);

    /**
     * 通过skuId查询到指定的sku_image的信息
     * @param skuId
     * @return
     */
    List<SkuImage> getSkuImageBySkuId(String skuId);

    /**
     * 根据skuId ，spuId 查询销售属性
     * @return
     * @param spuId
     * @param skuId
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySkuId(String spuId, String skuId);

    /**
     * 获取销售属性值Id集合
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpuId(String spuId);
}
