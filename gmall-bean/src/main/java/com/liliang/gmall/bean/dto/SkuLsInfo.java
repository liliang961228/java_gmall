package com.liliang.gmall.bean.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 放到elasticseacrh中的数据
 * @author liliang
 * @since 2019.6.16
 */
@Data
public class SkuLsInfo implements Serializable {

    String id;

    BigDecimal price;

    String skuName;
    // 商品描述
    //String skuDesc;

    String catalog3Id;

    String skuDefaultImg;

    // 热度排名
    Long hotScore=0L;

    // 平台属性值的集合
    List<SkuLsAttrValue> skuAttrValueList;

}
