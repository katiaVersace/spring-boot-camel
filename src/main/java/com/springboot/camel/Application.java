package com.springboot.camel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    @Value("${springboot.api.path}")
    String contextPath;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}
