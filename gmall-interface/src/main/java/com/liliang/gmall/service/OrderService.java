package com.liliang.gmall.service;

import com.liliang.gmall.bean.OrderInfo;
import com.liliang.gmall.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {
    /**
     * 自动生成流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 通过比较前台得到的订单号，防止页面重复提交
     * @param userId
     * @param tradeNo
     * @return
     */
    boolean checkTradeCode(String userId, String tradeNo);

    /**
     * 删除缓存中的订单号
     * @param userId
     */
    void removeTradeCode(String userId);

    /**
     * 保存订单详情
     *
     * @param orderInfo
     * @return
     */
    String saveOrderInfo(OrderInfo orderInfo);

    /**
     * 判断库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId, Integer skuNum);

    /**
     * 查询orderInfo信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(String orderId);

    /**
     * 清空已经下单成功的商品
     * @param userId
     */
    void clean(String userId);

    /**
     * 更新订单状态
     * @param orderId
     * @param paid
     */
    void updateOrderStatus(String orderId, ProcessStatus paid);

    /**
     * 发送消息给库存
     * @param orderId
     */
    void sendOrderStatus(String orderId);

    /**
     * 获取过期订单
     * @return
     */
    List<OrderInfo> getExpiredOrderList();

    /**
     * 处理过期订单
     * @param orderInfo
     */
    void execExpiredOrder(OrderInfo orderInfo);

    List<OrderInfo> splitOrder(String orderId, String wareSkuMap);

    Map initWareOrder(OrderInfo orderInfo);
}
