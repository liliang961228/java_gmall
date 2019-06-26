package com.liliang.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.liliang.gmall.bean.*;
import com.liliang.gmall.bean.enums.OrderStatus;
import com.liliang.gmall.bean.enums.ProcessStatus;
import com.liliang.gmall.config.LoginRequire;
import com.liliang.gmall.service.CartService;
import com.liliang.gmall.service.OrderService;
import com.liliang.gmall.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    @Reference
    private UserInfoService userInfoService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;

    /**
     * 提交选中商品，产生订单
     *trade
     * @param request
     * @return
     */
    @RequestMapping("trade")
    @LoginRequire
    public String toTrade(HttpServletRequest request) {
        //获取userID
        String userId = (String) request.getAttribute("userId");
        //根据userID获取用户的收货地址
        List<UserAddress> userAddressList = userInfoService.getUserAddress(userId);

        // 显示购物清单 显示orderDetail  从购物车中被选中的商品，赋值给orderDetail
        List<CartInfo> cartInfoChecked = cartService.getCartInfoChecked(userId);

        //创建一个订单详情集合
        ArrayList<OrderDetail> orderDetailList = new ArrayList<>();
        if (cartInfoChecked != null && cartInfoChecked.size() > 0) {
            for (CartInfo cartInfo : cartInfoChecked) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setOrderPrice(cartInfo.getSkuPrice());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetailList.add(orderDetail);
            }
        }


        //订单总金额
        OrderInfo orderInfo = new OrderInfo();
        // 将订单明细赋值给orderInfo
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());


        request.setAttribute("orderDetailArrayList", orderDetailList);
        request.setAttribute("userAddressList", userAddressList);

        //生成流水号
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo", tradeNo);
        return "trade";
    }


    /**
     * 提交订单将数据插入数据库：orderInfo ,orderDetail
     *
     * @param request
     * @param orderInfo
     * @return
     */
    @RequestMapping("submitOrder")
    @LoginRequire
    public String submirOrder(HttpServletRequest request, OrderInfo orderInfo) {

        String userId = (String) request.getAttribute("userId");
        orderInfo.setUserId(userId);

        //  总金额，订单状态，订单进度状态
        orderInfo.sumTotalAmount();
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);

        // 获页面的取流水号tradeNo
        String tradeNo = request.getParameter("tradeNo");
        // 比较流水号
        boolean flag = orderService.checkTradeCode(userId, tradeNo);
        if (!flag) {
            request.setAttribute("errMsg", "订单已经提交，请勿重复提交订单！");
            return "tradeFail";
        }

//         调用验证库存的方法,需要循环orderDetail 中的每个商品进行验证
//        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
//        if (orderDetailList!=null && orderDetailList.size()>0){
//            for (OrderDetail orderDetail : orderDetailList) {
//                //判断库存
//                boolean isflag = orderService.checkStock(orderDetail.getSkuId(),orderDetail.getSkuNum());
//
//                if (!isflag){
//                    request.setAttribute("errMsg",orderDetail.getSkuName()+"库存不足！");
//                    return "tradeFail";
//                }
//            }
//        }

        // 做保存
        String orderId = orderService.saveOrderInfo(orderInfo);

        // 删除缓存中的流水号
        orderService.removeTradeCode(userId);

        // 跳转到支付模块，支付模块会根据订单编号生成支付页面！
        return "redirect://payment.gmall.com/index?orderId=" + orderId;
        //http://payment.gmall.com/index?orderId=103
    }

    @RequestMapping("orderSplit")
    @ResponseBody
    public String orderSplit(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");
        // 定义订单集合
        List<OrderInfo> subOrderInfoList = orderService.splitOrder(orderId,wareSkuMap);
        List<Map> wareMapList=new ArrayList<>();
        for (OrderInfo orderInfo : subOrderInfoList) {
            Map map = orderService.initWareOrder(orderInfo);
            wareMapList.add(map);
        }
        return JSON.toJSONString(wareMapList);
    }










    @ResponseBody
    @RequestMapping("findAddressByUserId/{userId}")
    public int findAddressByUserId(@PathVariable String userId) {
        String id = userId;
        int i = userInfoService.findAddressByUserId(id);
        return i;

    }


    @ResponseBody
    @RequestMapping("findAll")
    public List<UserInfo> findAll() {
        List<UserInfo> all = userInfoService.findAll();
        return all;
    }

    @RequestMapping("getUserAddressNumberByUserId")
    public int getUserAddressNumberByUserId(String userId) {
        int i = userInfoService.getUserAddressNumberByUserId(userId);
        return i;
    }

}
