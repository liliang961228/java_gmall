package com.liliang.gmall.gmallmanageservice.mapper;

import com.liliang.gmall.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {

    /**
     * 根据spuid查询得到销售属性集合
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> spuSaleAttrListByspuId(String spuId);

    /**
     * 根据skuId ，spuId 查询销售属性
     * @param spuId
     * @param skuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySkuId(String spuId, String skuId);
}
