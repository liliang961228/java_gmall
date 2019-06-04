package com.liliang.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liliang.gmall.service.UserInfoService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Reference
    private UserInfoService userInfoService;

    @RequestMapping("findAddressByUserId/{id}")
    public UserAddress findAddressByUserId(@PathVariable String id){
        userInfoService.findAddressByUserId(id);
        return userInfoService.findAddressByUserId(id);
    }
}
