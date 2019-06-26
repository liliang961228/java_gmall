package com.liliang.gmall.bean.dto;

import lombok.Data;

import java.io.Serializable;

/**
 *封住前台传到后台的sku集合属性
 * @author liliang
 * @since 2019.6.16
 */
@Data
public class SkuLsParams implements Serializable {

    // keyword 相当于skuName
    String  keyword;

    String catalog3Id;

    String[] valueId;

    int pageNo=1;

    int pageSize=20;
}
