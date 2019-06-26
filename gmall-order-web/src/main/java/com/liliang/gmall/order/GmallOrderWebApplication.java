package com.liliang.gmall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.liliang.gmall")
public class GmallOrderWebApplication {

    public static void main(String[] args) {

        SpringApplication.run(GmallOrderWebApplication.class, args);
    }

}
