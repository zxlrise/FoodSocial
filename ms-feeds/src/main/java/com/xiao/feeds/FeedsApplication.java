package com.xiao.feeds;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.xiao.feeds.mapper")
@SpringBootApplication
public class FeedsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedsApplication.class, args);
    }

}
