package com.liliang.gmall.gmallorderservice.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@Configuration // 配置成xml
@EnableAsync // 开启异步调用
public class AsyOrderConfig implements AsyncConfigurer {

    // 从线程池中获取线程池执行者
    @Override
    public Executor getAsyncExecutor() {
        // 自行定义异常处理！ 自定义异常！
        // 获取线程池 – 数据库的连接池
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        // 设置线程数
        threadPoolTaskExecutor.setCorePoolSize(10);
        // 设置最大连接数
        threadPoolTaskExecutor.setMaxPoolSize(100);
        // 设置等待队列，如果10个不够，可以有100个线程等待 缓冲池
        threadPoolTaskExecutor.setQueueCapacity(100);
        // 初始化操作
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }

    // 定义异常处理！
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {

        return null;
    }
}
