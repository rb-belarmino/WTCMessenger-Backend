package com.wtcmessenger.worker;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Service
@Slf4j
public class MessageWorker {

    @KafkaListener(topics = "wtc.message.send", groupId = "wtc-messaging-group")
    public void consumeMessage(Map<String, Object> payload) {
        log.info("Recebendo evento Kafka wtc.message.send: {}", payload);
        
        // Simulating Firebase Cloud Messaging Push
        log.info("Simulating FCM Push for deep link to user device...");
        
        // Updating MongoDB to DELIVERED
        log.info("Atualizando status da mensagem no MongoDB para DELIVERED");
    }
}
