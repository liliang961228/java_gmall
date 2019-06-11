package com.liliang.gmall.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
public class SkuImage implements Serializable {

    @Id
    @Column
    String id;
    @Column
    String skuId;
    @Column
    String imgName;
    @Column
    String imgUrl;
    @Column
    String spuImgId;
    @Column
    String isDefault;

}
