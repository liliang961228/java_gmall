package com.liliang.gmall.gmallcartweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liliang.gmall.bean.CartInfo;
import com.liliang.gmall.bean.SkuInfo;
import com.liliang.gmall.config.LoginRequire;
import com.liliang.gmall.service.CartService;
import com.liliang.gmall.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    @Reference
    private ItemService itemService;

    //skuId,skuNum,userId
    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response) {

        //获取前台的参数值
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        String userId = (String) request.getAttribute("userId");


        //判断用户是否登录
        if (userId != null) {

            //用户已经登录，把数据放到数据库中同时更新redis
            cartService.addToCart(skuId, skuNum, userId);
        } else {
            //用户没有登录，把数据放到Cookie中
            cartCookieHandler.addToCookieCart(request, response, skuId, Integer.parseInt(skuNum), userId);
        }

        SkuInfo skuInfo = itemService.getSkuInfoBySkuId(skuId);

        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", skuNum);

        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response) {

        String userId = (String) request.getAttribute("userId");
        //根据userID的信息，判断用户是否登录
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (userId != null) {
            //已登录
            //判断cookie中有没有购物车的信息，
            // 如果有合并购物车，
            // 删除缓存
            List<CartInfo> cartInfoListCK = cartCookieHandler.getCartList(request);
            if (cartInfoListCK != null && cartInfoListCK.size() > 0) {
                //合并购物车
                cartInfoList = cartService.mergeToCartList(cartInfoListCK, userId);

                // 删除cookie中的购物车信息
                cartCookieHandler.deleteCartCookie(request, response);
            } else {
                cartInfoList = cartService.getCartList(userId);
            }

        } else {
            //未登录
            cartInfoList = cartCookieHandler.getCartList(request);

        }
        request.setAttribute("cartInfoList", cartInfoList);
        return "cartList";
    }

    /*
    同样这里要区分，用户登录和未登录状态。
    如果登录，修改缓存中的数据，如果未登录，修改cookie中的数据。
     */
    //http://cart.gmall.com/checkCart
    //checkCart
    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request, HttpServletResponse response) {

        String userId = (String) request.getAttribute("userId");
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");

        if (userId != null) {
            cartService.checkCart(skuId,userId,isChecked);
        }else {
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }

    }


    //http://cart.gmall.com/toTrade
    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request,HttpServletResponse response){

        String userId = (String) request.getAttribute("userId");

        List<CartInfo> cartListCK = cartCookieHandler.getCartList(request);
        if (cartListCK!=null && cartListCK.size()>0){
            cartService.mergeToCartList(cartListCK,userId);
            cartCookieHandler.deleteCartCookie(request,response);
        }

        return "redirect://order.gmall.com/trade";
    }
}
