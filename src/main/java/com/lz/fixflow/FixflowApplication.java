package com.lz.fixflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lz.fixflow.mapper")
public class FixflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(FixflowApplication.class, args);
    }

}
