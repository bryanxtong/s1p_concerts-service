package org.sp1.demo.concerts.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@SpringBootApplication
@EnableMongoRepositories
@EnableDiscoveryClient
public class ConcertsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConcertsServiceApplication.class, args);
    }

    @Configuration
    @EnableWebFluxSecurity
    public static class SecurityPermitAllConfig {
        @Bean
        public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
            http.authorizeExchange(authorizeExchangeSpec ->
                            authorizeExchangeSpec
                                    .anyExchange()
                                    .permitAll())
                    .csrf(csrf-> csrf.disable());
            return http.build();
        }
    }

}
