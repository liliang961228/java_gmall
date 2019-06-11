package com.liliang.gmall.gmallmanageservice.mapper;

import com.liliang.gmall.bean.BaseAttrInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    /**
     * 根据catalog3Id查询平台属性集合
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> attrInfoListByCatalog3Id(String catalog3Id);
}
