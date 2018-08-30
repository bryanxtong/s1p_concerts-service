package org.spring.cloud.k8s.concertsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoAuditing
@EnableMongoRepositories
public class ConcertsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConcertsServiceApplication.class, args);
	}
}