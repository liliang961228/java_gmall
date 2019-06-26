package com.liliang.gmall.gmallcartservice.mapper;

import com.liliang.gmall.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {

    List<CartInfo> selectAllList(String userId);
}
