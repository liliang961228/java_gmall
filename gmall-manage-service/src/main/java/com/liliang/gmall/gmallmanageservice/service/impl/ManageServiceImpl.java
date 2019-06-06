package com.liliang.gmall.gmallmanageservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.liliang.gmall.bean.*;
import com.liliang.gmall.gmallmanageservice.mapper.*;
import com.liliang.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
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
        if (baseAttrInfo.getAttrName().length()==0)
            return "error";


        //根据id判断是添加还是更新,对baseAttrInfo表的操作
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length()>0){

            //更新属性
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        }else{

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
        if (baseAttrValues!=null && baseAttrValues.size()>0){

            //循环遍历
            for (BaseAttrValue attrValue : baseAttrValues) {

                //判断属性值是否为空，如果为空就跳过本次循环
                if(attrValue.getValueName().length()==0)
                    continue;

                //设置主键为空，如果不设置数据库中的主键不会自增
                attrValue.setId(null);
                attrValue.setAttrId( baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }

        return "ok";

    }

    @Override
    public List<BaseAttrValue> getAttrValueListByAttrId(String attrId) {
        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId",attrId);
        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectByExample(example);
        return baseAttrValues;
    }
}
