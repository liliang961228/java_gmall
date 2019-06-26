package com.liliang.gmall.gmallpayment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class ProducerTest {

    /**
     * 1.创建一个工厂
     * 2.创建连接，打开连接
     * 3.创建session
     * 4.创建队列
     * 5.创建消息提供者
     * 6.发送消息
     * 7.关闭
     */

    public static void main(String[] args) throws JMSException {

        //创建一个消息中间件的连接工厂
        ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory("tcp://192.168.32.129:61616");

        //创建连接
        Connection connection = activeMQConnectionFactory.createConnection();
        //打开连接
        connection.start();

        //创建session
        // 第一个参数，true ：表示开启事务，false 表示不开启事务
        // 第二个参数，true/false 表示开启事务的方式， true==Session.SESSION_TRANSACTED
        Session session = connection.createSession(true,Session.AUTO_ACKNOWLEDGE);

        //创建队列
        Queue queue = session.createQueue("hello");

        //创建生产者
        MessageProducer producer = session.createProducer(queue);
        //创建消息对象
        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText("创建消息生产者");

        //发送消息
        producer.send(activeMQTextMessage);
        session.commit();

        //关闭连接
        producer.close();
        session.close();
        connection.close();
    }

}
