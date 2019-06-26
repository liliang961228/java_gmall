package com.liliang.gmall.gmallorderservice.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.liliang.gmall.bean.OrderDetail;
import com.liliang.gmall.bean.OrderInfo;
import com.liliang.gmall.bean.enums.OrderStatus;
import com.liliang.gmall.bean.enums.ProcessStatus;
import com.liliang.gmall.gmallorderservice.mapper.OrderDetailMapper;
import com.liliang.gmall.gmallorderservice.mapper.OrderInfoMapper;
import com.liliang.gmall.gmallserviceutil.config.ActiveMQUtil;
import com.liliang.gmall.gmallserviceutil.config.RedisUtil;
import com.liliang.gmall.service.CartService;
import com.liliang.gmall.service.OrderService;
import com.liliang.gmall.service.PaymentService;
import com.liliang.gmall.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Reference
    private CartService cartService;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Reference
    private PaymentService paymentService;
    /**
     * 自动生成流水号
     *
     * @param userId
     * @return
     */
    @Override
    public String getTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        // 定义key user:userId:tradeCode;
        String tradeNoKey = "user:" + userId + ":tradeCode";

        String tradeNo = UUID.randomUUID() + userId;
        jedis.set(tradeNoKey, tradeNo);
        jedis.close();
        return tradeNo;
    }

    /**
     * 通过比较前台得到的订单号，防止页面重复提交
     *
     * @param userId
     * @param tradeNo
     * @return
     */
    @Override
    public boolean checkTradeCode(String userId, String tradeNo) {
        Jedis jedis = redisUtil.getJedis();
        // 定义key user:userId:tradeCode;
        String tradeNoKey = "user:" + userId + ":tradeCode";
        //通过key，得到value
        String tradeNoValue = jedis.get(tradeNoKey);
        jedis.close();
        return tradeNo.equals(tradeNoValue);
    }

    /**
     * 删除缓存中的订单号
     *
     * @param userId
     */
    @Override
    public void removeTradeCode(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String key = "user:" + userId + ":tradeCode";
        jedis.del(key);
        jedis.close();
    }

    /**
     * 保存订单
     *
     * @param orderInfo
     * @return
     */
    @Override
    public String saveOrderInfo(OrderInfo orderInfo) {
        // 缺少
        //订单状态，
        orderInfo.setOrderStatus(OrderStatus.UNPAID);

        // 第三方交易编号，
        String outTradeNo = "GMALL" + UUID.randomUUID();
        orderInfo.setOutTradeNo(outTradeNo);

        // 创建时间，
        orderInfo.setCreateTime(new Date());

        // 过期时间，
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());

        // 进程状态，
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);

        //向数据库中插入订单表
        orderInfoMapper.insertSelective(orderInfo);

        //获取订单详情表，向订单详情表中插入订单详情
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (orderDetailList != null && orderDetailList.size() > 0) {
            for (OrderDetail orderDetail : orderDetailList) {

                orderDetail.setOrderId(orderInfo.getId());
                orderDetailMapper.insertSelective(orderDetail);
            }
        }


        return orderInfo.getId();
    }

    //根据sku判断是否有库存
    // http://www.gware.com/hasStock?skuId=skuId&num=skuNum

    /**
     * 判断库存
     *
     * @param skuId
     * @param skuNum
     * @return
     */
    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String s = HttpClientUtil.doGet("www.gware.com/hasStock?skuId=" + skuId + "&num=+" + skuNum);

        return "1".equals(s);
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo order = new OrderInfo();
        order.setId(orderId);
        OrderInfo orderInfo = orderInfoMapper.selectOne(order);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }

    /**
     * 清空已经下单成功的商品
     *
     * @param userId
     */
    @Override
    public void clean(String userId) {
        Jedis jedis = redisUtil.getJedis();
        // user:1:checked
        String userCheckedKey = "user:"+userId+":checked";

        jedis.del(userCheckedKey);

        jedis.close();

    }

    /**
     * 更新订单状态
     *
     * @param orderId
     * @param processStatus
     */
    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {

        // 修改状态
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    /**
     * 发送订单状态
     *
     * @param orderId
     */
    @Override
    public void sendOrderStatus(String orderId) {
        // 获取连接
        Connection connection = activeMQUtil.getConnection();

        String JsonStr = initWareOrder(orderId);
        try {
            connection.start();
            // 创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建对象
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            // 创建消息提供者
            MessageProducer producer = session.createProducer(order_result_queue);

            // 创建消息对象
            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            activeMQTextMessage.setText(JsonStr); // json 字符串
            producer.send(activeMQTextMessage);

            session.commit();
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取过期订单
     * @return
     */
    @Override
    public List<OrderInfo> getExpiredOrderList() {
        // 查询orderInfoList
        Example example = new Example(OrderInfo.class);
        // expireTime<new Date(); orderStatus = OrderStatus.UNPAID
        example.createCriteria().andLessThan("expireTime",new Date()).andEqualTo("processStatus", ProcessStatus.UNPAID);
        return orderInfoMapper.selectByExample(example);
    }

    /**
     * 处理过期订单
     * @param orderInfo
     */
    @Override
    @Async //使其变成异步！
    public void execExpiredOrder(OrderInfo orderInfo) {

        // 更新订单的状态
        updateOrderStatus(orderInfo.getId(),ProcessStatus.CLOSED);
        // 关闭交易记录表
        paymentService.closePayment(orderInfo.getId());
    }

    /**
     * 拆单
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    @Override
    public List<OrderInfo> splitOrder(String orderId, String wareSkuMap) {
        List<OrderInfo> subOrderInfoList = new ArrayList<>();
        // 1 先查询原始订单
        OrderInfo orderInfoOrigin = getOrderInfo(orderId);
        // 2 wareSkuMap 反序列化
        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);
        // 3 遍历拆单方案
        for (Map map : maps) {
            String wareId = (String) map.get("wareId");
            List<String> skuIds = (List<String>) map.get("skuIds");
            // 4 生成订单主表，从原始订单复制，新的订单号，父订单
            OrderInfo subOrderInfo = new OrderInfo();
            BeanUtils.copyProperties(subOrderInfo,orderInfoOrigin);
            subOrderInfo.setId(null);
            // 5 原来主订单，订单主表中的订单状态标志为拆单
            subOrderInfo.setParentOrderId(orderInfoOrigin.getId());
            subOrderInfo.setWareId(wareId);

            // 6 明细表 根据拆单方案中的skuids进行匹配，得到那个的子订单
            List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
            // 创建一个新的订单集合
            List<OrderDetail> subOrderDetailList = new ArrayList<>();
            for (OrderDetail orderDetail : orderDetailList) {
                for (String skuId : skuIds) {
                    if (skuId.equals(orderDetail.getSkuId())){
                        orderDetail.setId(null);
                        subOrderDetailList.add(orderDetail);
                    }
                }
            }
            subOrderInfo.setOrderDetailList(subOrderDetailList);
            subOrderInfo.sumTotalAmount();
            // 7 保存到数据库中
            saveOrderInfo(subOrderInfo);
            subOrderInfoList.add(subOrderInfo);
        }
        updateOrderStatus(orderId,ProcessStatus.SPLIT);
        // 8 返回一个新生成的子订单列表
        return subOrderInfoList;

    }


    // 制作json 字符串
    private String initWareOrder(String orderId) {
        // 根据orderId 获取orderInfo,orderDetail
        OrderInfo orderInfo = getOrderInfo(orderId);
        // 将orderInfo 做成一个map
        Map map = initWareOrder(orderInfo);

        return JSON.toJSONString(map);

    }

    @Override
    public Map initWareOrder(OrderInfo orderInfo) {

        // 创建map 集合
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel", orderInfo.getConsigneeTel());
        map.put("orderComment", orderInfo.getOrderComment());
        map.put("orderBody", "test-困困！要睡觉啦！");
        map.put("deliveryAddress", orderInfo.getDeliveryAddress());
        map.put("paymentWay", "2");
        map.put("wareId", orderInfo.getWareId());
        ArrayList<Map> orderMap = new ArrayList<>();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (orderDetailList != null && orderDetailList.size() > 0) {
            for (OrderDetail orderDetail : orderDetailList) {
                // {skuId:101,skuNum:1,skuName:’小米手64G’}
                HashMap<String, Object> detailMap = new HashMap<>();
                detailMap.put("skuId", orderDetail.getSkuId());
                detailMap.put("skuNum", orderDetail.getSkuNum());
                detailMap.put("skuName", orderDetail.getSkuName());
                // 把detailMap 放入一个集合中！
                orderMap.add(detailMap);
            }
        }
        map.put("details", orderMap);

        return map;
    }


}
