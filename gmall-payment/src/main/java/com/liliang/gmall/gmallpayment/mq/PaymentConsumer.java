package com.liliang.gmall.gmallpayment.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liliang.gmall.bean.PaymentInfo;
import com.liliang.gmall.service.PaymentService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PaymentConsumer {

    @Reference
    private PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        // mapMessage 来获取消息队列中的数据
        String outTradeNo = mapMessage.getString("outTradeNo");
        int delaySec = mapMessage.getInt("delaySec");
        int checkCount = mapMessage.getInt("checkCount");

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        // out_trade_no 给paymentInfo
        boolean flag = paymentService.checkPayment(paymentInfo);
        // 检查失败说明没有付款，检查的次数应该大于0
        System.out.println("检查结果："+flag);
        if (!flag && checkCount>0){
            // 再次发送消息
            System.out.println("检查次数"+checkCount);
            paymentService.sendDelayPaymentResult(outTradeNo,delaySec,checkCount-1);
        }



    }
}
