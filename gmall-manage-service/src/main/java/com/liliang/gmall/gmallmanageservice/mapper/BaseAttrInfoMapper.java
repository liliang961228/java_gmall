package com.liliang.gmall.gmallmanageservice.mapper;

import com.liliang.gmall.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    /**
     * 根据catalog3Id查询平台属性集合
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> attrInfoListByCatalog3Id (String catalog3Id);

    /**
     * 通过平台属性id，查询对应的平台属性
     * @param join
     * @return
     */
    List<BaseAttrInfo> selectBaseAttrInfoByValueIds(@Param("join") String join);
}
