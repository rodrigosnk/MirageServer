package com.example.MirageServer;

import org.jetbrains.annotations.Async;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class MirageServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MirageServerApplication.class, args);
	}

}
