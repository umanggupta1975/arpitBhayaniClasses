package com.umang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        // For Rabbit MQ
        System.setProperty("spring.amqp.deserialization.trust.all","true");
        SpringApplication.run(Main.class, args);
    }

}