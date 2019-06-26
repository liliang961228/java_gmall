package com.liliang.gmall.gmallorderservice.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liliang.gmall.bean.enums.ProcessStatus;
import com.liliang.gmall.service.OrderService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderConsumer {

    @Reference
    private OrderService orderService;

    // 获取消息监听器工厂
    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage) throws JMSException {
        // mapMessage 来获取消息队列中的数据
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");

        if ("success".equals(result)){
            // 更新订单的状态
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
            // 发送消息给库存
            orderService.sendOrderStatus(orderId);
            // 更新订单状态！
            orderService.updateOrderStatus(orderId,ProcessStatus.DELEVERED);

        }
    }
    // 获取消息监听器工厂
    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        // mapMessage 来获取消息队列中的数据
        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");

        if ("DEDUCTED".equals(status)){
            // 更新订单的状态
            orderService.updateOrderStatus(orderId, ProcessStatus.FINISHED);
        }
    }
}
