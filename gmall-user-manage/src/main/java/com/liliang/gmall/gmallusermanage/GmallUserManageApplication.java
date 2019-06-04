package com.liliang.gmall.gmallusermanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.liliang.gmall.gmallusermanage.mapper")
public class GmallUserManageApplication {

    public static void main(String[] args) {

        SpringApplication.run(GmallUserManageApplication.class, args);
    }

}
