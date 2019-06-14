package com.liliang.gmall.gmallserviceutil.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liliang
 * @since
 */
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:disable}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    @Bean
    public RedisUtil getRedisUtil(){

        //当host为disable时，host设置为null
        if ("disable".equals(host)){
            return null;
        }
        RedisUtil redisUtil = new RedisUtil();
        //初始化连接池
        redisUtil.initJedisPool(host,port,database);
        return redisUtil;
    }
}
