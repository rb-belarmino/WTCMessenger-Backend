package com.wtcmessenger.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    // For MongoDB save you would inject a MessageRepository here.

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> payload) {
        // Create pending intent in Mongo
        // Publish to Kafka
        payload.put("id", UUID.randomUUID().toString());
        payload.put("status", "PENDING");
        
        kafkaTemplate.send("wtc.message.send", payload);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Mensagem enviada com sucesso para processamento.",
            "data", payload
        ));
    }
}
