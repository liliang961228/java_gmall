package com.liliang.gmall.bean.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 返回到前台查询商品的商品结果集
 * @author liliang
 * @since 2019.6.16
 */
@Data
public class SkuLsResult implements Serializable {

    // 商品集合
    List<SkuLsInfo> skuLsInfoList;
    // 总条数
    long total;
    // 总页数
    long totalPages;
    // 平台属性值集合列表
    List<String> attrValueIdList;
}
