package com.liliang.gmall.service;

import com.liliang.gmall.bean.UserAddress;
import com.liliang.gmall.bean.UserInfo;

import java.util.List;

public interface UserInfoService {

    /**
     * 查询所有用户信息
     *
     * @return
     */
    List<UserInfo> findAll();

    /**
     * 根据用户Id 查询用户地址列表
     *
     * @param userId
     * @return
     */
    List<UserAddress> findAddressByUserId(String userId);

}
