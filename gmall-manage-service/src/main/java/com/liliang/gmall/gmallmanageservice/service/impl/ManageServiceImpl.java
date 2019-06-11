package com.liliang.gmall.gmallmanageservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.liliang.gmall.bean.*;
import com.liliang.gmall.gmallmanageservice.mapper.*;
import com.liliang.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Override
    public List<BaseCatalog1> getCatalog1() {

        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        return baseAttrInfoMapper.select(baseAttrInfo);
    }

    @Override
    public String saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        //判断baseAttrInfo是否为空，如果baseAttrInfo为空就直接返回，不在添加数据库
        if (baseAttrInfo.getAttrName().length() == 0)
            return "error";


        //根据id判断是添加还是更新,对baseAttrInfo表的操作
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {

            //更新属性
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        } else {

            //添加
            //设置主键为空，如果不设置数据库中的主键不会自增
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        // BaseAttrValue 需要更新：update！ 【delete insert！】
        // delete from baseAttrValue where attrId =  baseAttrInfo.id
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue);


        //对baseAttrValue表的操作
        List<BaseAttrValue> baseAttrValues = baseAttrInfo.getAttrValueList();

        //baseAttrValue添加表中
        if (baseAttrValues != null && baseAttrValues.size() > 0) {

            //循环遍历
            for (BaseAttrValue attrValue : baseAttrValues) {

                //判断属性值是否为空，如果为空就跳过本次循环
                if (attrValue.getValueName().length() == 0)
                    continue;

                //设置主键为空，如果不设置数据库中的主键不会自增
                attrValue.setId(null);
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }

        return "ok";

    }

    @Override
    public List<BaseAttrValue> getAttrValueListByAttrId(String attrId) {
        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId", attrId);
        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectByExample(example);
        return baseAttrValues;
    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);

        return spuInfoList;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {

        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public String saveSpuInfo(SpuInfo spuInfo) {

        //先判断spuInfo是否为空
        if (StringUtils.isEmpty(spuInfo.getSpuName()))
            return "error";

        //根据id判断是保存还是更新
        if (spuInfo.getId() != null && spuInfo.getId().length() > 0) {

            //更新
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);

        } else {

            //添加
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        }

        //商品图片的添加或者是更新
        //先删除，在添加或者更新
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();

        //删除商品的照片
        Example example = new Example(SpuImage.class);
        example.createCriteria().andEqualTo("spuId", spuInfo.getId());
        spuImageMapper.deleteByExample(example);

        //更新商品的照片或者的保存商品的照片
        if (spuImageList != null && spuImageList.size() > 0) {

            //添加商品的照片
            for (SpuImage spuImage : spuImageList) {

                spuImage.setId(null);
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }

        }

        //spu销售属性的添加或者是更新,
        //先删除在添加
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();

        //删除数据
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            String saleAttrId = spuSaleAttr.getSaleAttrId();
            String spuId = spuSaleAttr.getSpuId();

            //当符合条件是删除spu销售属性表
            Example spuSaleAttrExample = new Example(SpuSaleAttr.class);
            spuSaleAttrExample
                    .createCriteria()
                    .andEqualTo("spuId", spuId)
                    .andEqualTo("saleAttrId", saleAttrId)
                    .andEqualTo("saleAttrName", spuSaleAttr.getSaleAttrName());
            spuSaleAttrMapper.deleteByExample(spuSaleAttrExample);

            //删除spu销售属性值
            Example spuSaleAttrValueExample = new Example(SpuSaleAttrValue.class);
            spuSaleAttrValueExample
                    .createCriteria()
                    .andEqualTo("spuId", spuId)
                    .andEqualTo("saleAttrId", saleAttrId);
            spuSaleAttrValueMapper.deleteByExample(spuSaleAttrValueExample);
        }


        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {

            //添加销售属性表和基本销售属性表中的数据
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {

                //添加spu销售属性spuSaleAttr
                spuSaleAttr.setId(null);
                spuSaleAttr.setSpuId(spuSaleAttr.getSpuId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                //添加spu销售属性值表spuSaleAttrValue
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0) {
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {

                        spuSaleAttrValue.setId(null);
                        spuSaleAttrValue.setSpuId(spuSaleAttr.getSpuId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }


            }
        }

        return "ok";
    }
}
