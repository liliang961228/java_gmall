package com.liliang.gmall.gmallorderservice.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liliang.gmall.bean.OrderInfo;
import com.liliang.gmall.service.OrderService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@EnableScheduling
@Component
public class OrderTask {

    @Reference
    private OrderService orderService;

    // 分时日月周年
    // 每分钟的第五秒执行该方法
    @Scheduled(cron = "5 * * * * ?")
    public void testOne(){
        System.out.println(Thread.currentThread().getName()+"-------------");
    }
    // 每隔五秒执行一次
    @Scheduled(cron = "0/5 * * * * ?")
    public void testTwo(){
        System.out.println(Thread.currentThread().getName()+"=============");
    }

    // 定时扫描过期订单，并关闭
    @Scheduled(cron = "0/20 * * * * ?")
    public void checkOrder(){
        // 单线程处理！
        // 多线程处理!
       /*
        1.  先获取过期订单 过期时间<new Date() and  是未支付状态
        2.  循环处理过期订单
        3.  处理交易记录表
        */
        List<OrderInfo> orderInfoList =  orderService.getExpiredOrderList();
        if (orderInfoList!=null && orderInfoList.size()>0){
            // 循环处理过期订单
            for (OrderInfo orderInfo : orderInfoList) {
                // 处理单个过期订单对象
                orderService.execExpiredOrder(orderInfo);
            }
        }
    }

}
