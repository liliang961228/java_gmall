package com.liliang.gmall.gmallpayment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.liliang.gmall.bean.PaymentInfo;
import com.liliang.gmall.bean.enums.PaymentStatus;
import com.liliang.gmall.gmallpayment.mapper.PaymentInfoMapper;
import com.liliang.gmall.gmallserviceutil.config.ActiveMQUtil;
import com.liliang.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Autowired
    private AlipayClient alipayClient;

    /**
     *保存订单信息
     * @param paymentInfo
     */
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);

    }

    /**
     * 得到订单的信息
     * @param out_trade_no
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfo(String out_trade_no) {
        // select * fronm paymentInfo where out_trade_no = ?[out_trade_no]
        PaymentInfo paymentInfo  = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        return paymentInfoMapper.selectOne(paymentInfo);
    }

    /**
     * 更新订单的信息
     * @param out_trade_no
     * @param paymentInfoUpd
     */
    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd) {

        // 设置更新条件
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);

        paymentInfoMapper.updateByExampleSelective(paymentInfoUpd,example);
    }

    /**
     * 更新订单的状态
     * @param paymentInfo
     * @param result
     */
    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {

        Connection connection = activeMQUtil.getConnection();
        // 打开连接
        try {
            connection.start();
            // 创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建 队列

            Queue payment_result_queue = session.createQueue("PAYMENT_RESULT_QUEUE");
            // 创建消息提供者
            MessageProducer producer = session.createProducer(payment_result_queue);
            // 创建消息对象
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("orderId",paymentInfo.getOrderId());
            activeMQMapMessage.setString("result",result);

            producer.send(activeMQMapMessage);
            // 提交消息
            session.commit();
            // 关闭
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * 支付宝订单状态查询
     * @param paymentInfoQuery
     * @return
     */
    @Override
    public boolean checkPayment(PaymentInfo paymentInfoQuery) {
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        // 将第三方交易编号传入查询接口
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfoQuery.getOutTradeNo());
        request.setBizContent(JSON.toJSONString(map));
//        request.setBizContent("{" +
//                "\"out_trade_no\":\"20150320010101001\"," +
//                "\"trade_no\":\"2014112611001004680 073956707\"," +
//                "\"org_pid\":\"2088101117952222\"," +
//                "      \"query_options\":[" +
//                "        \"[\\\"TRADE_SETTE_INFO\\\"]\"" +
//                "      ]" +
//                "  }");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        // 表示改订单在支付宝中存在！
        if(response.isSuccess()){
            // trade_status TRADE_SUCCESS TRADE_FINISHED
            if ("TRADE_SUCCESS".equals(response.getTradeStatus()) || "TRADE_FINISHED".equals(response.getTradeStatus())){
                System.out.println("调用成功");
                // 修改交易记录的状态！
                // update paymentInfo set payment_status = PaymentStatus.PAID where out_trade_no = paymentInfoQuery.getOutTradeNo()
                PaymentInfo paymentInfoUPD = new PaymentInfo();
                paymentInfoUPD.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfo(paymentInfoQuery.getOutTradeNo(),paymentInfoUPD);
                // 发送支付成功的消息给订单！
                sendPaymentResult(paymentInfoQuery,"success");

                return true;
            }

        } else {
            System.out.println("调用失败");
        }
        return false;
    }

    /**
     * 延迟队列反复调用
     * @param outTradeNo 单号
     * @param delaySec 延迟秒
     * @param checkCount 几次
     */
    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {

        // 获取连接
        Connection connection = activeMQUtil.getConnection();

        try {
            connection.start();
            // 创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue payment_result_check_queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            // 创建消息提供者
            MessageProducer producer = session.createProducer(payment_result_check_queue);
            // 创建消息对象
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("outTradeNo",outTradeNo);
            activeMQMapMessage.setInt("delaySec",delaySec);
            activeMQMapMessage.setInt("checkCount",checkCount);
            // 开启延迟队列设置
            activeMQMapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);

            producer.send(activeMQMapMessage);
            // 必须提交
            session.commit();

            // 关闭
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭过期订单
     * @param orderId
     */
    @Override
    public void closePayment(String orderId) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId",orderId);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);

    }
}
