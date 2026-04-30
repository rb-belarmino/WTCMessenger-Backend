package com.wtcmessenger.worker;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.firebase.FirebaseService;
import com.wtcmessenger.model.Message;
import com.wtcmessenger.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWorker {

    private final FirebaseService firebaseService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.message-send}",
            groupId = "wtc-messaging-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void processMessage(
            ConsumerRecord<String, Object> record,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        MessengerDTO.MessageSendEvent event = objectMapper.convertValue(
                record.value(), MessengerDTO.MessageSendEvent.class);

        log.info("Processando mensagem: messageId={}, recipientId={}, partition={}, offset={}",
                event.getMessageId(), event.getRecipientId(), partition, offset);

        try {
            String fcmMessageId = firebaseService.sendPushNotification(event);

            messageService.updateStatus(event.getMessageId(), Message.MessageStatus.DELIVERED, null);

            log.info("Mensagem entregue via FCM: messageId={}, fcmMessageId={}",
                    event.getMessageId(), fcmMessageId);

        } catch (FirebaseMessagingException ex) {
            log.error("Falha FCM para messageId={}: {}", event.getMessageId(), ex.getMessage());
            throw new RuntimeException("Firebase delivery failed: " + ex.getMessage(), ex);

        } catch (Exception ex) {
            log.error("Erro inesperado ao processar messageId={}: {}", event.getMessageId(), ex.getMessage(), ex);
            throw ex;

        } finally {
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.dead-letter}",
            groupId = "wtc-messaging-dlq-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeadLetter(
            ConsumerRecord<String, Object> record,
            Acknowledgment acknowledgment) {

        try {
            MessengerDTO.MessageSendEvent event = objectMapper.convertValue(
                    record.value(), MessengerDTO.MessageSendEvent.class);

            log.error("Mensagem na DLQ (falha definitiva): messageId={}, recipientId={}",
                    event.getMessageId(), event.getRecipientId());

            messageService.updateStatus(
                    event.getMessageId(),
                    Message.MessageStatus.FAILED,
                    "Falha definitiva após esgotamento de todos os retries."
            );

        } catch (Exception e) {
            log.error("Erro ao processar mensagem da DLQ: {}", e.getMessage(), e);
        } finally {
            acknowledgment.acknowledge();
        }
    }
}
