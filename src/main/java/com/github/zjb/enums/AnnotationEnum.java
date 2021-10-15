package com.github.zjb.enums;

public enum AnnotationEnum {
    FEIGN_CLIENT("FeignClient", "org.springframework.cloud.openfeign.FeignClient"),
    KAFKA_LISTENER("FeignClient", "org.springframework.kafka.annotation.KafkaListener"),
    ROCKETMQ_MESSAGE_LISTENER("RocketMQMessageListener", "org.apache.rocketmq.spring.annotation.RocketMQMessageListener"),
    VALUE("Value", "org.springframework.beans.factory.annotation.Value"),
    SCHEDULED("Scheduled", "org.springframework.scheduling.annotation.Scheduled");

    private String name;
    private String qualifiedName;


    AnnotationEnum(String name, String qualifiedName) {
        this.name = name;
        this.qualifiedName = qualifiedName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }
}