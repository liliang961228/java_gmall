package com.liliang.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liliang.gmall.bean.UserAddress;
import com.liliang.gmall.bean.UserInfo;
import com.liliang.gmall.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {

    @Reference
    private UserInfoService userInfoService;

    @ResponseBody
    @RequestMapping("findAddressByUserId/{userId}")
    public List<UserAddress> findAddressByUserId(@PathVariable String userId){
        userInfoService.findAddressByUserId(userId);
        return userInfoService.findAddressByUserId(userId);
    }


    @ResponseBody
    @RequestMapping("findAll")
    public List<UserInfo> findAll(){
        List<UserInfo> all = userInfoService.findAll();
        return all;
    }

}
