package com.example.SpringWithMySql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
//@EnableScheduling
//@EnableAsync
public class SpringWithMySqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringWithMySqlApplication.class, args);
	}
	@Bean
	public CacheManager cacheManager() {
		ConcurrentMapCacheManager cacheManager= new ConcurrentMapCacheManager("book"); 
		return cacheManager;
	}

}
