package com.liliang.gmall.service;

import com.liliang.gmall.bean.*;

import java.util.List;

public interface ManageService {

    /**
     * 查询所有的一级标题
     * @return
     */
    public List<BaseCatalog1> getCatalog1();

    /**
     * 查询一级标题下的二级标题
     * @param catalog1Id
     * @return
     */
    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 查询二级标题下的三级标题
     * @param catalog2Id
     * @return
     */
    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 查询三级标题下的属性名称
     * @param catalog3Id
     * @return
     */
    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    /**
     * 添加或者更新，属性和属性的属性名称
     * @param baseAttrInfo
     */
    String saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 通过属性id得到属性值
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueListByAttrId(String attrId);

    /**
     * 查询商品表
     * @param spuInfo
     * @return
     */
    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    /**
     * 查询到所有的所有的销售属性
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存商品信息和销售属性
     * @param spuInfo
     * @return
     */
    String saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuId查找指定的图片信息
     * @param spuId
     * @return
     */
    List<SpuImage> spuImageList(String spuId);

    /**
     * 根据spuId查询spu销售属性集合
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> spuSaleAttrList(String spuId);

    /**
     * 根据catalog3Id查询平台属性集合
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> attrInfoListByCatalog3Id(String catalog3Id);

    /**
     * 保存skuInfo的值
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);


    /**
     * 通过平台属性id，查询对应的平台属性
     * @param attrValueIdList
     * @return
     */
    List<BaseAttrInfo> getBaseAttrByValueIds(List<String> attrValueIdList);
}