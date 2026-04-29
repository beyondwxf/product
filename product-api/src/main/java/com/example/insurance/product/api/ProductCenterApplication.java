package com.example.insurance.product.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 保险产品中心 Spring Boot 启动类。
 */
@SpringBootApplication(scanBasePackages = "com.example.insurance.product")
public class ProductCenterApplication {
    /**
     * 应用启动入口。
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductCenterApplication.class, args);
    }
}
