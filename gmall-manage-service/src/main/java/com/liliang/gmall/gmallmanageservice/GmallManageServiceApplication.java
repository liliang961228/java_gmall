package com.liliang.gmall.gmallmanageservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.liliang.gmall.gmallmanageservice.mapper")
@ComponentScan(basePackages = "com.liliang.gmall")
public class GmallManageServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(GmallManageServiceApplication.class, args);
    }

}
