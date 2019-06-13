package com.liliang.gmall.gmallmanageservice.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.liliang.gmall.bean.SkuImage;
import com.liliang.gmall.bean.SkuInfo;
import com.liliang.gmall.bean.SkuSaleAttrValue;
import com.liliang.gmall.bean.SpuSaleAttr;
import com.liliang.gmall.gmallmanageservice.mapper.SkuImageMapper;
import com.liliang.gmall.gmallmanageservice.mapper.SkuInfoMapper;
import com.liliang.gmall.gmallmanageservice.mapper.SkuSaleAttrValueMapper;
import com.liliang.gmall.gmallmanageservice.mapper.SpuSaleAttrMapper;
import com.liliang.gmall.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * 商品详情查找
 * @author liliang
 * @since 2019.06.12
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    /**
     * 通过skuId查询到指定的sku_Info的信息
     * @param skuId
     * @return SkuInfo
     */
    @Override
    public SkuInfo getSkuInfoBySkuId(String skuId) {

        return skuInfoMapper.selectByPrimaryKey(skuId);

    }

    /**
     * 通过skuId查询到指定的sku_image的信息
     * @param skuId
     * @return
     */
    @Override
    public List<SkuImage> getSkuImageBySkuId(String skuId) {

        Example example = new Example(SkuImage.class);
        example.createCriteria().andEqualTo("skuId",skuId);

        List<SkuImage> skuImageList = skuImageMapper.selectByExample(example);

        return skuImageList;
    }

    /**
     * 根据skuId ，spuId 查询销售属性
     * @return
     * @param spuId
     * @param skuId
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySkuId(String spuId, String skuId) {

        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrListCheckBySkuId(spuId,skuId);
        return spuSaleAttrList;
    }

    /**
     * 获取销售属性值Id集合
     * @param spuId
     * @return
     */
    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpuId(String spuId) {

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpuId(spuId);

        return skuSaleAttrValueList;
    }
}
