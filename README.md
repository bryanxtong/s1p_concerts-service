# S1P :: Concerts Service

This version supports "Spring Cloud Kubernetes Config Server" and Spring Cloud Kubernetes Configuration Watcher"

It will use spring cloud config to load data from configmap via configserver
and config watcher supports the congfigmap changes which will be then applied
to the target application.

With the way of using "Spring Cloud Kubernetes Config Server" is completely optional and with it, you have to deploy
the following in the k8s cluster

spring-cloud-kubernetes-configserver.yml
spring-cloud-kubernetes-configuration-watcher.yml





