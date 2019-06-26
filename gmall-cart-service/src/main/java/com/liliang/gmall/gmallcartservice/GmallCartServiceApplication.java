package com.liliang.gmall.gmallcartservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan("com.liliang.gmall")
@MapperScan("com.liliang.gmall.gmallcartservice.mapper")
public class GmallCartServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(GmallCartServiceApplication.class, args);
    }

}
