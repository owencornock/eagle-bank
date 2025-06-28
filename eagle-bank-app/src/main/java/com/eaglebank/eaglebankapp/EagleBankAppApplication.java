package com.eaglebank.eaglebankapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(
        scanBasePackages = "com.eaglebank",
        exclude = {
                UserDetailsServiceAutoConfiguration.class
        }
)
@EntityScan(basePackages = "com.eaglebank.eaglebankrepository")
@EnableJpaRepositories(basePackages = "com.eaglebank.eaglebankrepository")
public class EagleBankAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(EagleBankAppApplication.class, args);
    }
}

