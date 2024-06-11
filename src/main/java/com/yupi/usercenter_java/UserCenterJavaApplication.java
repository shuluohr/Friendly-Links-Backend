package com.yupi.usercenter_java;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yupi.usercenter_java.mapper")
public class UserCenterJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterJavaApplication.class, args);
    }

}
