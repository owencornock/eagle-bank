package com.eaglebank.eaglebankrepository;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.eaglebank.eaglebankrepository")
@EnableJpaRepositories(basePackages = "com.eaglebank.eaglebankrepository")
@ComponentScan(basePackages = "com.eaglebank.eaglebankrepository")
public class TestConfig {
}