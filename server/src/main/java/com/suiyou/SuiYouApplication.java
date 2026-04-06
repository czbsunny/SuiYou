package com.suiyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.suiyou.repository")
@EntityScan(basePackages = "com.suiyou.model")
@EnableAsync
@EnableScheduling
public class SuiYouApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuiYouApplication.class, args);
    }
}