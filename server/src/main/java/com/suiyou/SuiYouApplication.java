package com.suiyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.suiyou.repository")
@EnableAsync
public class SuiYouApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuiYouApplication.class, args);
    }
}