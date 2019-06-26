package com.liliang.gmall.gmallcartservice.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.liliang.gmall.bean.CartInfo;
import com.liliang.gmall.bean.SkuInfo;
import com.liliang.gmall.gmallcartservice.constant.CartConst;
import com.liliang.gmall.gmallcartservice.mapper.CartInfoMapper;
import com.liliang.gmall.gmallserviceutil.config.RedisUtil;
import com.liliang.gmall.service.CartService;
import com.liliang.gmall.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ItemService itemService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 查询缓存中所有的以被选中的商品
     *
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartInfoChecked(String userId) {
        Jedis jedis = redisUtil.getJedis();

        String key = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;

        List<String> cartInfoString = jedis.hvals(key);

        ArrayList<CartInfo> cartInfoList = new ArrayList<>();

        if (cartInfoString != null && cartInfoString.size() > 0) {

            for (String cartInfo : cartInfoString) {

                CartInfo cartInfoObject = JSON.parseObject(cartInfo, CartInfo.class);

                cartInfoList.add(cartInfoObject);

            }
        }
        return cartInfoList;
    }

    /**
     * 更新商品的选中状态
     *
     * @param skuId
     * @param userId
     * @param isChecked
     */
    @Override
    public void checkCart(String skuId, String userId, String isChecked) {

        Jedis jedis = redisUtil.getJedis();
        String key = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        //通过key查询到商品信息
        String cartInfo = jedis.hget(key, skuId);
        //修改选中状态
        CartInfo info = JSON.parseObject(cartInfo, CartInfo.class);
        info.setIsChecked(isChecked);
        jedis.hset(key, skuId, JSON.toJSONString(info));

        String checkedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;

        if ("1".equals(isChecked)) {

            jedis.hset(checkedKey, skuId, JSON.toJSONString(info));
        } else {
            jedis.hdel(checkedKey, skuId);
        }

        jedis.close();

    }

    /**
     * 用户已经登录，添加购物车
     *
     * @param skuId
     * @param skuNum
     * @param userId
     */
    @Override
    public void addToCart(String skuId, String skuNum, String userId) {

        /*
        1.判断数据库中是否有该条要添加到购物车的商品
           2.1如果有：数据库中的skuNum加1。
           2.2如果没有：添加商品信息到购物车
        3.把商品信息添加的缓存中
         */
        // 1
        CartInfo cartInfoDB = new CartInfo();
        cartInfoDB.setSkuId(skuId);
        cartInfoDB.setUserId(userId);
        CartInfo cartInfo = cartInfoMapper.selectOne(cartInfoDB);

        // 2
        if (cartInfo != null) {

            //2.1
            cartInfo.setSkuNum(cartInfo.getSkuNum() + Integer.parseInt(skuNum));
            cartInfo.setSkuPrice(cartInfo.getCartPrice());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfo);
        } else {

            //2.2
            CartInfo info = new CartInfo();
            SkuInfo skuInfo = itemService.getSkuInfoBySkuId(skuId);
            info.setSkuPrice(skuInfo.getPrice());
            info.setSkuNum(Integer.valueOf(skuNum));
            info.setCartPrice(skuInfo.getPrice());
            info.setSkuId(skuId);
            info.setImgUrl(skuInfo.getSkuDefaultImg());
            info.setSkuName(skuInfo.getSkuName());
            info.setUserId(userId);

            //添加商品信息到数据库中
            cartInfoMapper.insertSelective(info);
            cartInfo = info;
        }

        //  3
        Jedis jedis = redisUtil.getJedis();
        // 定义key user:userId:cart
        String key = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        jedis.hset(key, skuId, JSON.toJSONString(cartInfo));

        // 购物车---是否有过期时间！有！可以给用户的过期时间
        // 获取用户过期时间
        String userkey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
        //获取userkey的过期时间
        Long ttl = jedis.ttl(userkey);
        //设置cartkey的过期时间
        jedis.expire(key, ttl.intValue());
        //关闭redis连接
        jedis.close();

    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        /*
        1.  先查询缓存 如果缓存中存储，直接返回数据
        2.  如果缓存中没有数据，则从[数据库--实时价格查询]中查询，并放入缓存
         */

        List<CartInfo> cartInfos = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();
        String jedisKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        List<String> cartInfoString = jedis.hvals(jedisKey);

        //判断redis中是否有购物车的信息
        if (cartInfoString != null && cartInfoString.size() > 0) {

            for (String cartInfo : cartInfoString) {
                CartInfo cart = JSON.parseObject(cartInfo, CartInfo.class);
                cartInfos.add(cart);
            }

            //根据id排序，
            cartInfos.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });

            System.out.println("购物车信息没有查询数据库，直接查询了redis");
            //从缓存中查到数据后，直接返回数据
            return cartInfos;

        } else {

            //redis中没有存储cartinfo的任何信息，
            // 需要从数据库中查询，
            // 然后再放回缓存
            cartInfos = this.selectAllList(userId);
            System.out.println("从数据库中查询到了信息");
            return cartInfos;
        }

    }

    /**
     * 合并购物车信息
     *
     * @param cartInfoListCK
     * @param userId
     */
    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartInfoListCK, String userId) {

        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId", userId);
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectByExample(example);

        List<CartInfo> cartInfoList = new ArrayList<>();

        //判断数据库中是否有购物车信息，
        // 如果有合并，
        if (cartInfoListDB != null && cartInfoListDB.size() > 0) {

            for (CartInfo cartInfoCk : cartInfoListCK) {
                boolean isFlag = false;
                for (CartInfo cartInfoDB : cartInfoListDB) {
                    if (cartInfoDB.getSkuId().equals(cartInfoCk.getSkuId())) {
                        cartInfoDB.setSkuNum(cartInfoCk.getSkuNum() + cartInfoDB.getSkuNum());
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                        isFlag = true;
                    }
                }
                if (!isFlag) {
                    cartInfoCk.setUserId(userId);
                    cartInfoMapper.insertSelective(cartInfoCk);
                }
            }

            // 再将更新的数据与添加的数据 查询出来。
            cartInfoList = this.selectAllList(userId);

        } else {
            // 没有就直接添加
            for (CartInfo cartInfoCK : cartInfoListCK) {
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
            // 再将更新的数据与添加的数据 查询出来。
            cartInfoList = this.selectAllList(userId);
        }

        // 开始合并勾选状态的购物车
        for (CartInfo cartInfoDB : cartInfoList) {
            for (CartInfo cartInfoCK : cartInfoListCK) {
                if (cartInfoDB.getSkuId().equals(cartInfoCK.getSkuId())) {
                    if ("1".equals(cartInfoCK.getIsChecked())) {
                        // 需要将数据库中对象IsChecked改为 1
                        cartInfoDB.setIsChecked("1");
                        // 勾选商品！
                        checkCart(cartInfoCK.getSkuId(), "1", userId);
                    }
                }
            }
        }

        return cartInfoList;

    }

    /**
     * 清除已经下订单的购物车商品
     * @param cartInfoObject
     */
    @Override
    public void clean(CartInfo cartInfoObject) {
        cartInfoMapper.deleteByPrimaryKey(cartInfoObject);
    }

    // 需要从数据库中查询，
    // 然后再放回缓存
    @Override
    public List<CartInfo> selectAllList(String userId) {
        List<CartInfo> cartInfos = cartInfoMapper.selectAllList(userId);

        //判断数据库中的购物车时候有数据
        if (cartInfos != null && cartInfos.size() == 0) {
            return null;
        }
        Jedis jedis = redisUtil.getJedis();
        String jedisKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //定义一个map集合，用来存储商品的信息
        Map<String, String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfos) {

            //在map中把查询到的数据放入
            map.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
        }

        //把购物车信息放到redis中
        jedis.hmset(jedisKey, map);
        jedis.close();
        return cartInfos;
    }
}
