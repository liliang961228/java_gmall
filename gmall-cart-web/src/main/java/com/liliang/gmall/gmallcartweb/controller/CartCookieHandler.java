package com.liliang.gmall.gmallcartweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.liliang.gmall.bean.CartInfo;
import com.liliang.gmall.bean.SkuInfo;
import com.liliang.gmall.service.ItemService;
import com.liliang.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {

    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;

    @Reference
    private ItemService itemService;

    /**
     * 更新商品的选中状态
     * @param request
     * @param response
     * @param skuId
     * @param isChecked
     */
    public void checkCart(HttpServletRequest request,HttpServletResponse response, String skuId, String isChecked) {
        List<CartInfo> cartList = this.getCartList(request);
        if (cartList!=null&& cartList.size()>0){
            for (CartInfo cartInfo : cartList) {
                if (cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setIsChecked(isChecked);
                }
            }
        }


        CookieUtil.setCookie(request,response,cookieCartName,JSON.toJSONString(cartList),COOKIE_CART_MAXAGE,true);


    }


    public void addToCookieCart(HttpServletRequest request, HttpServletResponse response, String skuId, int skuNum, String userId) {

        /*
        前置条件：获取cookie中的商品，判断cookie中是否为空，
        为空，直接添加

        不为空：用一个集合来存cookie中的信息
        1.判断cookie中是否有该商品
        2.1 有：修改数量，价格
        2.2 没有：添加商品到cookie中
         */
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);

        // 获取出来的数据，是单独一个商品，还是有很多的商品？ cartJson 转换为cartInfo 的集合
        List<CartInfo> cartInfoList = new ArrayList<>();

        Boolean isFlag = false;

        //cookie不为空
        if (StringUtils.isNotEmpty(cookieValue)){
            cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);

            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());

                    isFlag=true;
                    break;
                }
            }
        }

        //为空
        if (!isFlag){
            CartInfo cartInfo = new CartInfo();
            SkuInfo skuInfo = itemService.getSkuInfoBySkuId(skuId);

            // 属性赋值
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);

            // 将cartInfo 放入cookie
            cartInfoList.add(cartInfo);
        }
        // 将修改之后的cartInfoList 放入cookie
        CookieUtil.setCookie(request,response,cookieCartName, JSON.toJSONString(cartInfoList),COOKIE_CART_MAXAGE,true);


    }

    /**
     * 未登录，查询cookie中的所有商品信息
     * @param request
     * @return
     */
    public List<CartInfo> getCartList(HttpServletRequest request) {

        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);

        List<CartInfo> cartInfos = JSON.parseArray(cookieValue, CartInfo.class);
        return cartInfos;


    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {

        CookieUtil.deleteCookie(request,response,cookieCartName);
    }
}
