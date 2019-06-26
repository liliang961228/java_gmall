package com.liliang.gmall.service;

import com.liliang.gmall.bean.CartInfo;

import java.util.List;

/**
 * 购物车操作接口
 * @author liliang
 * @since
 */
public interface CartService {

    /**
     * 查询redis中的购物车选中的商品
     * @param userId
     * @return
     */
    List<CartInfo> getCartInfoChecked(String userId);

    /**
     * 更新redis中选中商品的数量
     * @param skuId
     * @param userId
     * @param isChecked
     */
    void checkCart(String skuId, String userId, String isChecked);
    /**
     * 用户登录，添加购物车
     * @param skuId
     * @param skuNum
     * @param userId
     */
    void addToCart(String skuId, String skuNum, String userId);

    /**
     * 用户登录后，查询购物车的所有信息
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);

    /**
     *合并购物车
     * @param cartInfoListCK
     * @param userId
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartInfoListCK, String userId);

    /**
     * 清除购物车中已经下单的商品
     * @param cartInfoObject
     */
    void clean(CartInfo cartInfoObject);

    /**
     * 查询所有的购物车商品
     * @param userId
     * @return
     */
    List<CartInfo> selectAllList(String userId);
}
