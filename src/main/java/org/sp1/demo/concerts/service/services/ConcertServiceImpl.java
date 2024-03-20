package org.sp1.demo.concerts.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sp1.demo.concerts.service.config.ConcertsConfiguration;
import org.sp1.demo.concerts.service.model.Concert;
import org.sp1.demo.concerts.service.repo.ConcertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.kubernetes.discovery.KubernetesReactiveDiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

@Service
@RefreshScope
public class ConcertServiceImpl implements ConcertService {

    private static final Logger log = LoggerFactory.getLogger(ConcertServiceImpl.class);

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private KubernetesReactiveDiscoveryClient discoveryClient;

    @Autowired
    private ConcertsConfiguration config;

    @Autowired
    private ReactorLoadBalancerExchangeFilterFunction lbFunction;

    @Override
    public Mono<Concert> createConcert(Concert concert) {
        if (concert.getConcertDate() == null) {
            concert.setConcertDate(new Date());
        }
        return concertRepository.insert(concert);
    }


    @Override
    public Flux<Concert> findAll() {
        Flux<Concert> allConcerts = concertRepository.findAll();

        log.info("Decorate all Services enabled?: " + config.getDecorate());
        Boolean decorate = new Boolean(config.getDecorate());
        if (decorate) {
            // For each concert:
            //  - look for a ticket service with the concert name
            //  -- If found, get the amount of available tickets
            //  -- decorate the concert with the available tickets
/*            List<Concert> concerts = allConcerts.toStream().collect(Collectors.toList());
            concerts.forEach(concert -> findMatchingTicketsService(concert)
                    .ifPresent(serviceInstance -> decorateConcertWithTicketsInfo(concert, serviceInstance)));
            return Flux.fromIterable(concerts);*/
            return allConcerts.flatMap(concert -> {
                return findMatchingTicketsService(concert)
                        .flatMap(serviceInstances -> {
                            if (serviceInstances.size() > 0) {
                                return decorateConcertWithTicketsInfo(concert, serviceInstances.get(0));
                            } else {
                                return decorateConcertWithTicketsInfo(concert, null);
                            }
                        });
            });
        }
        return allConcerts;
    }

    @Override
    public Mono<Concert> updateConcert(Concert concert, String id) {
        return findOne(id).doOnSuccess(findConcert -> {
            findConcert.setConcertDate(concert.getConcertDate());
            findConcert.setName(concert.getName());
            findConcert.setBand(concert.getBand());
            findConcert.setCode(concert.getCode());
            concertRepository.save(findConcert).subscribe();
        });
    }

    @Override
    public Mono<Concert> findOne(String id) {
        // Get the concert from the local Repo
        Mono<Concert> concertMono = concertRepository.findById(id).
                switchIfEmpty(Mono.error(new Exception("No Concert found with Id: " + id)));
        return concertMono.doOnSuccess(concert ->
                findMatchingTicketsService(concert)
                        .flatMap(serviceInstances -> {
                            if (serviceInstances.size() > 0) {
                                return decorateConcertWithTicketsInfo(concert, serviceInstances.get(0));
                            } else {
                                return decorateConcertWithTicketsInfo(concert, null);
                            }
                        }));

    }

    private Mono<List<ServiceInstance>> findMatchingTicketsService(Concert concert) {
        Flux<String> services = discoveryClient.getServices();
        // Get service instance to check for extra metadata to bind concert code with tickets services
        log.info("> Finding Matching Tickets service for code: " + concert.getCode());
        Flux<ServiceInstance> serviceInstanceFlux = services
                .flatMap(service -> discoveryClient.getInstances(service))
                .filter(instance -> concert.getCode().equals(instance.getMetadata().get("code")));
        Mono<List<ServiceInstance>> listMono = serviceInstanceFlux.collectList();
        return listMono;

    }

    private Mono<Concert> decorateConcertWithTicketsInfo(Concert concert, ServiceInstance ticketsServiceForConcert) {

        if (ticketsServiceForConcert == null){
            return Mono.just(concert);
        }
        log.info("> Decorating Concert with Service : " + ticketsServiceForConcert.getServiceId());
        return Mono.just(ticketsServiceForConcert).flatMap(serviceInstance -> {
            WebClient webClient = WebClient.builder()
                    .baseUrl("http://" + serviceInstance.getServiceId())
                    .filter(lbFunction)
                    .build();
            Mono<Integer> availableTickets = webClient
                    .get()
                    .uri("/tickets")
                    .retrieve()
                    .bodyToMono(Integer.class);
            Mono<Concert> map = availableTickets.map(ti -> {
                log.info("> Available Tickets for " + concert.getName() + ": " + ti);
                concert.setAvailableTickets(ti.toString());
                return concert;
            });
            return map;
        });
    }

    @Override
    public Flux<Concert> findByName(String name) {
        return concertRepository.findByName(name)
                .switchIfEmpty(Mono.error(new Exception("No Concert found with name Containing : " + name)));
    }

    @Override
    public Mono<Void> delete(String id) {
        return concertRepository.deleteById(id);
    }
}
