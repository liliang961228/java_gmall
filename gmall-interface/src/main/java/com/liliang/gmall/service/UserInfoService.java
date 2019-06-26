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
    int findAddressByUserId(String userId);

    /**
     * 根据用户id统计地址信息的个数
     * @param userId
     * @return
     */
    int getUserAddressNumberByUserId(String userId);

    /**
     *  根据登录名称和密码查询是否存在
     * @param userInfo
     * @return
     */
    UserInfo getUserInfoByNamePwd(UserInfo userInfo);

    /**
     * 认证
     * @param userId
     * @return
     */
    UserInfo verify(String userId);

    /**
     * 通过用户的id查找用户的收货地址
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddress(String userId);
}
