package com.liliang.gmall.gmallusermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.liliang.gmall.bean.UserAddress;
import com.liliang.gmall.bean.UserInfo;
import com.liliang.gmall.gmallserviceutil.config.RedisUtil;
import com.liliang.gmall.gmallusermanage.mapper.UserAddressMapper;
import com.liliang.gmall.gmallusermanage.mapper.UserInfoMapper;
import com.liliang.gmall.service.UserInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;
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
    public int findAddressByUserId(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        userAddress.setUserAddress("北京市大兴区");

        int i = userAddressMapper.insertSelective(userAddress);
        return i;
    }

    @Override
    public int getUserAddressNumberByUserId(String userId) {
        Example example = new Example(UserAddress.class);
        example.createCriteria().andEqualTo("userId",userId);
        int i = userAddressMapper.selectCountByExample(example);
        return i;
    }

    @Override
    public UserInfo getUserInfoByNamePwd(UserInfo userInfo) {

        String passwd = userInfo.getPasswd();
        //明文转换成密文
        String s = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(s);
        UserInfo userInfo1 = userInfoMapper.selectOne(userInfo);

        if (userInfo1!=null){

            Jedis jedis = redisUtil.getJedis();
            String key = userKey_prefix+userInfo1.getId()+userinfoKey_suffix;
            jedis.setex(key,userKey_timeOut, JSON.toJSONString(userInfo1));
            jedis.close();
            System.out.println("userInfo======="+userInfo1.getId());
            return userInfo1;
        }

        return null;

    }

    /**
     * 认证
     * @param userId
     * @return
     */
    @Override
    public UserInfo verify(String userId) {
        // key=user:1:info
        // 获取jedis
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String key = userKey_prefix+userId+userinfoKey_suffix;
        String userJson = jedis.get(key);
        if (StringUtils.isNotEmpty(userJson)){
            jedis.expire(userJson,userKey_timeOut);

            // userJson 将其转换为对象
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }

        return null;
    }

    @Override
    public List<UserAddress> getUserAddress(String userId) {
        Example example = new Example(UserAddress.class);
        example.createCriteria().andEqualTo("userId",userId);
        List<UserAddress> userAddresses = userAddressMapper.selectByExample(example);
        return userAddresses;
    }
}
