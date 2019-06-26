package com.liliang.gmall.gmallpayment.mq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class ConsumerTest {

    /*
        1.  创建消息队列工厂
        2.  创建连接打开连接
        3.  创建session
        4.  创建队列
        5.  创建消息消费者
        6.  获取消息
         */
    public static void main(String[] args) throws JMSException {
        //创建消息队列工厂
        ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,
                        ActiveMQConnection.DEFAULT_PASSWORD, "tcp://192.168.32.129:61616");
        //创建连接
        Connection connection = activeMQConnectionFactory.createConnection();
        //打开连接
        connection.start();
        //创建session
        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
        //创建队列
        Queue queue = session.createQueue("hello");
        //创建消息消费者
        MessageConsumer consumer = session.createConsumer(queue);
        //获取消息
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message instanceof ActiveMQTextMessage){
                    try {
                        String text = ((ActiveMQTextMessage) message).getText();
                        System.out.println("接受到的消息是："+text);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
