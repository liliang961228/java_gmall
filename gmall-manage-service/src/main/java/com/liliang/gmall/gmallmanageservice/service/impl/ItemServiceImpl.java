package com.liliang.gmall.gmallmanageservice.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.liliang.gmall.bean.*;
import com.liliang.gmall.gmallmanageservice.constant.ItemConst;
import com.liliang.gmall.gmallmanageservice.mapper.*;
import com.liliang.gmall.gmallserviceutil.config.RedisUtil;
import com.liliang.gmall.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * 商品详情查找
 * @author liliang
 * @since 2019.06.12
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    /**
     * 从rides中查询到指定的数据，如果redis中没有指定的数据先从DB中查找，
     * 然后再把查找到的只定数据放到redis中去
     * @param skuId
     * @return SkuInfo
     */
    @Override
    public SkuInfo getSkuInfoBySkuId(String skuId) {

        //定义jedis对象
        Jedis jedis=null;

        try {
            //获取jedis对象
            jedis = redisUtil.getJedis();

            //拼接要查找的key,sku:skuId:info
            String jediskey = ItemConst.SKUKEY_PREFIX+skuId+ItemConst.SKUKEY_SUFFIX;
            String skuInfo = jedis.get(jediskey);
            if (skuInfo==null || skuInfo.length()==0){

                //缓存redis中没有数据，需要去数据库中查询，把查询到的数据放到redis缓存中
                System.out.println("缓存中没有数据，要去数据库中查询，" +
                        "同时为了解决缓存击穿要把key加锁！！！");
                //需要加锁的key
                String lockSkuKey = ItemConst.SKUKEY_PREFIX+skuId+ItemConst.SKULOCK_SUFFIX;

                //把可以上锁
                //  窗口命令set k2 v2 px 10000 nx
                //String lockKey = jedis.set(lockSkuKey, "ok", "NX", "PX", ItemConst.SKULOCK_EXPIRE_PX);
                String lockKey  = jedis.set(lockSkuKey, "OKok", "NX", "PX", ItemConst.SKULOCK_EXPIRE_PX);
                if ("OK".equals(lockKey)){

                    //获取数据，并放入缓存
                    System.out.println("获取分布式锁！并查询数据放入缓存！");
                    SkuInfo skuInfoLock = getSkuInfoBySkuIdBD(skuId);

                    //判断skuInfoLock是否为空
                    if (skuInfoLock!=null){
                        //把查询到的数据放到redis缓存中
                        jedis.setex(jediskey, ItemConst.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfoLock));
                    }

                    return skuInfoLock;
                }else {

                    //其它线程等待
                    Thread.sleep(10*1000);
                    //等待完成在去自己调用自己去查询
                    return getSkuInfoBySkuId(skuId);
                }
            }else {

                //缓存中有数据，把json传转换成指定的实体对象，返回给对象
                SkuInfo skuInfo1 = JSON.parseObject(skuInfo, SkuInfo.class);
                return skuInfo1;
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {

            //判断jedis是否为空，不为空就关闭jedis连接，
            // （防止出现空指针异常）
            if (jedis!=null){

                jedis.close();
            }
        }

        return getSkuInfoBySkuIdBD(skuId);

    }

    /**
     * 从数据库中查到指定的skuId的数据
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoBySkuIdBD(String skuId){

        //通过skuId查询到指定的shuInfo数据
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);

        //查询商品的图片信息，把图片信息放到redis中
        List<SkuImage> skuImages = this.getSkuImageBySkuId(skuId);
        skuInfo.setSkuImageList(skuImages);

        //查询商品的属性值，把商品的属性值放到skuInfo中
        List<SkuAttrValue> skuAttrValues = this.getSkuAttrValueBySkuId(skuId);
        skuInfo.setSkuAttrValueList(skuAttrValues);

        return skuInfo;

    }

    /**
     * 通过skuId查询到指定的一组skuAttrValue的信息
     * @param skuId
     * @return
     */
    public List<SkuAttrValue> getSkuAttrValueBySkuId(String skuId){

        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        return skuAttrValueList;
    }

    /**
     * 通过skuId查询到指定的sku_image的信息
     * @param skuId
     * @return
     */
    @Override
    public List<SkuImage> getSkuImageBySkuId(String skuId) {

        Example example = new Example(SkuImage.class);
        example.createCriteria().andEqualTo("skuId",skuId);

        List<SkuImage> skuImageList = skuImageMapper.selectByExample(example);

        return skuImageList;
    }

    /**
     * 根据skuId ，spuId 查询销售属性
     * @return
     * @param spuId
     * @param skuId
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySkuId(String spuId, String skuId) {

        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrListCheckBySkuId(spuId,skuId);
        return spuSaleAttrList;
    }

    /**
     * 获取销售属性值Id集合
     * @param spuId
     * @return
     */
    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpuId(String spuId) {

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpuId(spuId);

        return skuSaleAttrValueList;
    }
}
