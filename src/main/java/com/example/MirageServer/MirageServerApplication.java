package com.example.MirageServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MirageServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MirageServerApplication.class, args);
	}

}
