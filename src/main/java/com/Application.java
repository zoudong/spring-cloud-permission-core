package com;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


/**
 * 主启动类
 * 扫描mybatis mapper org的换成tk的包
 */
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableWebMvc
@EnableScheduling
@EnableTransactionManagement
@MapperScan("com.zoudong.permission.mapper")
@ComponentScan(basePackages = {"com.zoudong.permission"})
@SpringBootApplication
@EnableAutoConfiguration
public class Application {
    public static void main(String[] args) throws Exception {
        SpringApplication springApplication = new SpringApplication(Application.class);
        //springApplication.addListeners();
        SpringApplication.run(Application.class, args);
    }
}