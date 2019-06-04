package com.liliang.gmall.gmallusermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.liliang.gmall.bean.UserAddress;
import com.liliang.gmall.bean.UserInfo;
import com.liliang.gmall.gmallusermanage.mapper.UserAddressMapper;
import com.liliang.gmall.gmallusermanage.mapper.UserInfoMapper;
import com.liliang.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    /**
     * 查询所有的用户
     *
     * @return
     */
    @Override
    public List<UserInfo> findAll() {

        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> findAddressByUserId(String userId) {

        System.out.println("id+++++++++++++"+userId);
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> userAddresses = userAddressMapper.select(userAddress);
        for (UserAddress address : userAddresses) {
            System.out.println("address++++++++++"+address);
        }
        return userAddresses;
    }
}
