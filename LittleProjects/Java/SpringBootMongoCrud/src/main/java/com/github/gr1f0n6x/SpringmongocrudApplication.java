package com.github.gr1f0n6x;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "com.github.gr1f0n6x")
@SpringBootApplication
public class SpringmongocrudApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringmongocrudApplication.class, args);
	}
}
