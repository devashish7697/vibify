package com.vibify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VibifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(VibifyApplication.class, args);
	}

}
