package com.rollup.rollupapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RollupApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RollupApiApplication.class, args);
    }

}
