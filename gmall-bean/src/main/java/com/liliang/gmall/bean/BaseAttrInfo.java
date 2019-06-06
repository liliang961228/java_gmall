package com.liliang.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
public class BaseAttrInfo implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)   //主键自增
    private String id;
    @Column
    private String attrName;
    @Column
    private String catalog3Id;

    @Transient    // 平台属性值集合    @Transient //表示数据库中没有的字段，但是在业务中需要！
    private List<BaseAttrValue> attrValueList;


}
