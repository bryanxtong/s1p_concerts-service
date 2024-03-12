package org.sp1.demo.concerts.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
@EnableMongoRepositories
@EnableDiscoveryClient
public class ConcertsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConcertsServiceApplication.class, args);
    }

    @Configuration
    @EnableWebSecurity
    public static class SecurityPermitAllConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(authorizeHttpRequests ->
                            authorizeHttpRequests
                                    .anyRequest()
                                    .permitAll())
                    .csrf(AbstractHttpConfigurer::disable);
            return http.build();
        }
    }

}
