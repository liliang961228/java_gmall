package com.liliang.gmall.gmallserviceutil.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis工具类
 * @author liliang
 * @since 2019.6.14
 */
public class RedisUtil {

    //创建一个连接池
    private JedisPool jedisPool = null;

    /**
     * 创建一个初始化方法
     */
    public void initJedisPool(String host,int port,int database){

        //创建一个jedispool的连接参数对象
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        //连接池的最大连接数
        jedisPoolConfig.setMaxTotal(500);

        //设置连接池的最长等待时间
        jedisPoolConfig.setMaxWaitMillis(10*1000);

        //设置连接池最小剩余连接数
        jedisPoolConfig.setMinIdle(20);

        //设置连接池开机自检，检查当前连接是否可用
        jedisPoolConfig.setTestOnBorrow(true);

        //设置连接池达到最大的连接数，是否需要等待
        jedisPoolConfig.setBlockWhenExhausted(true);

        //实例化jedisPool对象
        jedisPool = new JedisPool(jedisPoolConfig, host, port, 10000);
    }

    //获取Jides
    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
