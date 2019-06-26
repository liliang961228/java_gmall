package com.liliang.gmall.gmallitemweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.liliang.gmall")
public class GmallItemWebApplication {

    public static void main(String[] args) {

        SpringApplication.run(GmallItemWebApplication.class, args);

    }

}
