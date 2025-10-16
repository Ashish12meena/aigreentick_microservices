package com.aigreentick.services.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
public class CommonCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommonCoreApplication.class, args);
	}

}
