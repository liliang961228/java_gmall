package com.liliang.gmall.gmallpayment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.liliang.gmall.bean.OrderInfo;
import com.liliang.gmall.bean.PaymentInfo;
import com.liliang.gmall.bean.enums.PaymentStatus;
import com.liliang.gmall.config.LoginRequire;
import com.liliang.gmall.gmallpayment.config.AlipayConfig;
import com.liliang.gmall.service.OrderService;
import com.liliang.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

@Controller
public class PaymentController {

    @Reference
    private PaymentService paymentService;

    @Reference
    private OrderService orderService;

    @Autowired
    private AlipayClient alipayClient;


    //http://payment.gmall.com/index?orderId=103
    @RequestMapping("index")
    @LoginRequire
    public String index(@RequestParam String orderId, HttpServletRequest request) {

        request.setAttribute("orderId", orderId);

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        BigDecimal totalAmount = orderInfo.getTotalAmount();
        request.setAttribute("totalAmount", totalAmount);

        return "index";
    }

    //http://payment.gmall.com/alipay/submit
    @RequestMapping("alipay/submit")
    @LoginRequire
    @ResponseBody
    public String submit(String orderId, HttpServletRequest request, HttpServletResponse response) {

        /*
        1.  生产一个二维码，进行支付
        2.  保存一下交易记录 为什么需要记录订单信息！paymentInfo
         */
        // 第三方交易编号，orderid，总金额，主题，支付状态，创建时间
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setSubject("测试支付！");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setOrderId(orderId);

        //保存支付的订单
        paymentService.savePaymentInfo(paymentInfo);

        // https://openapi.alipay.com/gateway.do 固定网关
        // 将以下参数放入配置文件中！ AlipayClient  放入到spirng 容器中！<bean class="" />
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //获得初始化的AlipayClient
        //创建API对应的request
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();

        //同步回调路径
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);

        //https://www.domain.com/CallBack/return_url?out_trade_no=ATGUIGU1561171028531599&version=1.0&app_id=2018020102122556&charset=utf-8&sign_type=RSA2&trade_no=2019062222001484560562234789&auth_app_id=2018020102122556&timestamp=2019-06-22%2011:57:21&seller_id=2088921750292524&method=alipay.trade.page.pay.return&total_amount=0.01&sign=cFJtO9n1F0iLP3Oy/ZiyjiLGb9Ng3J3OOoTe3eFnDmxGNRclGmrDR4hJb6jc98/uerYYeizQ5+BSZrngayi2WPLWayMusbNHKFYhPInZuc1ZY0VoA8+u9DrZQbe9rqgBIMUIp9Zy3w3WQNsVOiszOz5kk58eqhI0giLuP3RAYRYY5pbw2g1zBjmFWW+eFjH0gXclEV7KlVrwlg4h098mQw/qltyTTCXBTqH35yd8H965mgLNg4OYPByUW60ZGCqJIxrBOXbBc8Wx0OACmAKiLg/BK2ZKGt2UF6JXh72JaHn2fibz7Bt4Y9XsmguzPv/BibJ0bW7SemwmNeTqnPykYg==
        //异步回调路径
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", paymentInfo.getOutTradeNo());
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", paymentInfo.getTotalAmount());
        map.put("subject", paymentInfo.getSubject());
        alipayRequest.setBizContent(JSON.toJSONString(map));

//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\"20150320010101001\"," +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                "    \"total_amount\":88.88," +
//                "    \"subject\":\"Iphone6 16G\"," +
//                "    \"body\":\"Iphone6 16G\"," +
//                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
//                "    \"extend_params\":{" +
//                "    \"sys_service_provider_id\":\"2088511833207846\"" +
//                "    }"+
//                "  }");//填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
//        response.getWriter().write(form);//直接将完整的表单html输出到页面
//        response.getWriter().flush();
//        response.getWriter().close();

        // 发送消息
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);

        return form;
    }

    /**
     * 同步回调
     * http://payment.gmall.com/alipay/callback/return?
     * charset=utf-8&
     * out_trade_no=GMALLfa94cf7b-3558-4952-bb3b-dda416d0f2c6&
     * method=alipay.trade.page.pay.return&
     * total_amount=0.01&
     * sign=bv8xhte8heZaBS1uCJOj5STZNVC2u2a%2BX4y3fGI3v5tc%2Fo61IxFg%2B9PSRbbZEMbrliTTbJS%2FTEVg%2FveAqrHzk9%2BVazQb2m72wrOL7P%2FxLLmzZbqxsJ2sva7p6IXfPjbJqgvH6KYpSOw75N8RybALzXa2FH5Yuwhn6Mk3KNMVaQw1lzs7xkEaY3mWG6Jhi4uyNgrJh%2BXrPmVORaw8HsIjXpNFyqDaMtJdcN9cI3JSBBpR2fR4Lq2CAzr7IPJgZDrfyPNX5JQwq9bjFNUkJ4gpe9fvJB%2B%2BeruLjk40xu9UY6B3REmTu8QUF0fmCSSIYfy5Asors51BwjS1BoTqrWIgqQ%3D%3D&
     * trade_no=2019062422001400050563887939&
     * auth_app_id=2018020102122556&
     * version=1.0&
     * app_id=2018020102122556&sign_type=RSA2&seller_id=2088921750292524&timestamp=2019-06-24+22%3A32%3A24
     */
    @RequestMapping("alipay/callback/return")
    public String callbackReturn(HttpServletRequest request) {
        // 重定向到订单列表 http://order.gmall.com/trade
        // 清空购物车：
        String userId = (String) request.getAttribute("userId");
        // 订单清空购物车
        orderService.clean(userId);
        return "redirect:" + AlipayConfig.return_order_url;
    }


    /**
     * 异步回调
     */
    @RequestMapping("alipay/callback/notify")
    public String callbackNotify(@RequestParam HashMap paramsMap) {
// Map<String, String> paramsMap = ... //将异步通知中收到的所有参数都存放到map中
        try {
            //调用SDK验证签名
            boolean flag = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
            if (flag) {
                // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
                // 业务内容进行二次校验！
                String trade_status = (String) paramsMap.get("trade_status");
                if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
                    // 再确认一步，才能修改以下两种状态！outTradeNo

                    // 获取到电商交易记录中的状态！
                    String out_trade_no = (String) paramsMap.get("out_trade_no");
                    // 通过out_trade_no 查询交易状态 select * fronm paymentInfo where out_trade_no = ?[out_trade_no]
                    PaymentInfo paymentInfo = paymentService.getPaymentInfo(out_trade_no);
                    // 在交易记录表中，查询一下交易的状态，paymentStatus ，如果说交易状态为PAID，ClOSED 说明当前订单已经被交易过了，或者是关闭了。
                    if (paymentInfo.getPaymentStatus() == PaymentStatus.PAID || paymentInfo.getPaymentStatus() == PaymentStatus.ClOSED) {
                        return "failure";
                    }
                    // 记录交易成功，修改paymentInfo 中的paymentStatus = PAID支付状态
                    // update paymentInfo set paymentStatus=PAID where out_trade_no = ?[out_trade_no]
                    PaymentInfo paymentInfoUpd = new PaymentInfo();
                    paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                    paymentInfoUpd.setCallbackTime(new Date());

                    paymentService.updatePaymentInfo(out_trade_no, paymentInfoUpd);

                    // 更新订单的状态 orderStatus=PAID 应该是由OrderService。
                    paymentService.sendPaymentResult(paymentInfo,"success");
                    return "success";
                }

            } else {
                // TODO 验签失败则记录异常日志，并在response中返回failure.
                return "failure";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return "failure";
    }


    // sendPaymentResult?orderId=99&result=success
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,String result){

        paymentService.sendPaymentResult(paymentInfo,result);
        return "sendPaymentResult";
    }

    // 查询订单信息
    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOrderId(orderId);
        boolean flag = paymentService.checkPayment(paymentInfoQuery);
        return ""+flag;
    }



}