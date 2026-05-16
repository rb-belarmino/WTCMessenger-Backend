package com.wtcmessenger.worker;

import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.firebase.FirebaseService;
import com.wtcmessenger.model.Message;
import com.wtcmessenger.repository.MessageRepository;
import com.wtcmessenger.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CampaignWorker {

    private final MessageRepository messageRepository;
    private final FirebaseService firebaseService;
    private final CampaignService campaignService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.campaign-dispatch}",
            groupId = "wtc-messaging-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void processCampaignDispatch(
            ConsumerRecord<String, Object> record,
            Acknowledgment acknowledgment) {

        MessengerDTO.CampaignDispatchEvent event = objectMapper.convertValue(
                record.value(), MessengerDTO.CampaignDispatchEvent.class);

        log.info("Disparando campanha: campaignId={}, recipientId={}",
                event.getCampaignId(), event.getRecipientId());

        try {
            Message message = Message.builder()
                    .senderId("system")
                    .recipientId(event.getRecipientId())
                    .campaignId(event.getCampaignId())
                    .content(event.getContent())
                    .deepLink(event.getDeepLink())
                    .type(Message.MessageType.CAMPAIGN)
                    .status(Message.MessageStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            message = messageRepository.save(message);

            MessengerDTO.MessageSendEvent pushEvent = MessengerDTO.MessageSendEvent.builder()
                    .messageId(message.getId())
                    .recipientId(event.getRecipientId())
                    .content(event.getContent())
                    .deepLink(event.getDeepLink())
                    .type(Message.MessageType.CAMPAIGN)
                    .campaignId(event.getCampaignId())
                    .sentAt(message.getSentAt())
                    // Token FCM vem do perfil do cliente (integração Membro 2)
                    .fcmToken("token-obtido-do-customer-service")
                    .build();

            firebaseService.sendPushNotification(pushEvent);

            message.setStatus(Message.MessageStatus.DELIVERED);
            message.setDeliveredAt(LocalDateTime.now());
            messageRepository.save(message);

            log.info("Push de campanha entregue: campaignId={}, recipientId={}",
                    event.getCampaignId(), event.getRecipientId());

        } catch (Exception ex) {
            log.error("Falha no disparo da campanha {}, recipientId={}: {}",
                    event.getCampaignId(), event.getRecipientId(), ex.getMessage(), ex);
            throw new RuntimeException(ex);

        } finally {
            acknowledgment.acknowledge();
            campaignService.reconcileCampaignCounters(event.getCampaignId());
        }
    }
}
