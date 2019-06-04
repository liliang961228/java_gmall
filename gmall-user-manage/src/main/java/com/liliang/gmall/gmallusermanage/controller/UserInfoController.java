package com.liliang.gmall.gmallusermanage.controller;

import com.liliang.gmall.bean.UserInfo;
import com.liliang.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 查询所有的用户
     * @return
     */
    @RequestMapping("findAll")
    public List<UserInfo> findAll(){
        List<UserInfo> list = userInfoService.findAll();
        return list;
    }
}
