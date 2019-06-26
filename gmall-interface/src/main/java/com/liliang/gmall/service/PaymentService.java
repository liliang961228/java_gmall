package com.liliang.gmall.service;

import com.liliang.gmall.bean.PaymentInfo;

/**
 * 支付接口
 * @author liliang
 * @since
 */
public interface PaymentService {

    /**
     *保存支付订单信息
     * @param paymentInfo
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 得到订单的信息
     * @param out_trade_no
     * @return
     */
    PaymentInfo getPaymentInfo(String out_trade_no);

    /**
     * 更新订单的信息
     * @param out_trade_no
     * @param paymentInfoUpd
     */
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    /**
     * 更新订单状态
     * @param paymentInfo
     * @param result
     */
    void sendPaymentResult(PaymentInfo paymentInfo, String result);

    /**
     * 支付宝订单状态查询
     * @param paymentInfoQuery
     * @return
     */
    boolean checkPayment(PaymentInfo paymentInfoQuery);

    /**
     * 延迟队列反复调用
     * @param outTradeNo 单号
     * @param delaySec 延迟秒
     * @param checkCount 几次
     */
    void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount);

    /**
     * 关闭过期订单
     * @param id
     */
    void closePayment(String id);
}
