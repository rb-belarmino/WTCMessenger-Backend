package com.wtcmessenger.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FirebaseService {

    public String sendPushNotification(MessengerDTO.MessageSendEvent event) throws FirebaseMessagingException {

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase não inicializado. Simulando envio para recipientId={}", event.getRecipientId());
            return "simulated-fcm-id-" + event.getMessageId();
        }

        Map<String, String> data = buildDataPayload(event);

        Notification notification = Notification.builder()
                .setTitle(resolveTitle(event))
                .setBody(event.getContent())
                .build();

        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setSound("default")
                        .setBadge(1)
                        .setContentAvailable(true)
                        .build())
                .build();

        Message firebaseMessage = Message.builder()
                .setToken(event.getFcmToken())
                .setNotification(notification)
                .setApnsConfig(apnsConfig)
                .putAllData(data)
                .build();

        String messageId = FirebaseMessaging.getInstance().send(firebaseMessage);
        log.info("Push enviado: messageId={}, fcmMessageId={}", event.getMessageId(), messageId);

        return messageId;
    }

    public void sendSilentStatusUpdate(String fcmToken, String messageId,
                                       Message.MessageStatus status) {
        if (FirebaseApp.getApps().isEmpty()) return;

        try {
            Map<String, String> data = new HashMap<>();
            data.put("type", "STATUS_UPDATE");
            data.put("messageId", messageId);
            data.put("status", status.name());

            Message silentMessage = Message.builder()
                    .setToken(fcmToken)
                    .putAllData(data)
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder().setContentAvailable(true).build())
                            .build())
                    .build();

            FirebaseMessenger.getInstance().send(silentMessage);

        } catch (FirebaseMessengerException e) {
            log.warn("Falha no status update silencioso para messageId={}: {}", messageId, e.getMessage());
        }
    }

    private Map<String, String> buildDataPayload(MessengerDTO.MessageSendEvent event) {
        Map<String, String> data = new HashMap<>();
        data.put("messageId", event.getMessageId());
        data.put("type", event.getType().name());
        data.put("recipientId", event.getRecipientId());

        if (event.getDeepLink() != null && !event.getDeepLink().isBlank()) {
            data.put("deepLink", event.getDeepLink());
        }
        if (event.getCampaignId() != null) {
            data.put("campaignId", event.getCampaignId());
        }

        return data;
    }

    private String resolveTitle(MessengerDTO.MessageSendEvent event) {
        return switch (event.getType()) {
            case CHAT         -> "WTC Messenger";
            case CAMPAIGN     -> "WTC — Oferta Especial";
            case NOTIFICATION -> "WTC — Notificação";
        };
    }
}
