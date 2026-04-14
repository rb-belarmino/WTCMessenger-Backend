package com.wtcmessenger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WtcMessengerAplication {

    public static void main(String[] args) {
        SpringApplication.run(WtcMessengerAplication.class, args);
    }
}