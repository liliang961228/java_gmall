package com.liliang.gmall.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

@Data
public class BaseAttrValue implements Serializable {

    @Id
    @Column
    private String id;


    @Column
    private String valueName;
    @Column
    private String attrId;

    // 制作一个字段，专门用来记录最新的urlParam
    @Transient
    private String urlParam;

}
