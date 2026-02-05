package com.mptourism;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MpTourismBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MpTourismBackendApplication.class, args);
	}

}
