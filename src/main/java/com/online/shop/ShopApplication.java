package com.online.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ShopApplication.class);
//        app.addListeners(new ApplicationPidFileWriter(), new WebServerPortFileWriter());
        app.run(args);
    }

}
