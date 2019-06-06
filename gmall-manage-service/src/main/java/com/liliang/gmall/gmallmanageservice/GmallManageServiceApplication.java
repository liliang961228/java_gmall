package com.liliang.gmall.gmallmanageservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.liliang.gmall.gmallmanageservice.mapper")
public class GmallManageServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(GmallManageServiceApplication.class, args);
    }

}
