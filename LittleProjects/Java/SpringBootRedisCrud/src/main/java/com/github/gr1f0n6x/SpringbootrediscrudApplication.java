package com.github.gr1f0n6x;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableRedisRepositories
@SpringBootApplication
public class SpringbootrediscrudApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootrediscrudApplication.class, args);
	}
}
