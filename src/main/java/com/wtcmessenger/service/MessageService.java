package com.wtcmessenger.service;

import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.exception.MessageNotFoundException;
import com.wtcmessenger.model.Message;
import com.wtcmessenger.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.message-send}")
    private String messageSendTopic;

    @Value("${kafka.topics.message-status}")
    private String messageStatusTopic;

    public MessengerDTO.MessageResponse sendMessage(String senderId,
                                                    MessengerDTO.SendMessageRequest request,
                                                    String fcmToken) {
        validateSendRequest(request);

        Message message = Message.builder()
                .senderId(senderId)
                .recipientId(request.getRecipientId())
                .segmentId(request.getSegmentId())
                .content(request.getContent())
                .deepLink(request.getDeepLink())
                .type(request.getType())
                .mediaUrls(request.getMediaUrls())
                .status(Message.MessageStatus.SENT)
                .sentAt(LocalDateTime.now())
                .fcmToken(fcmToken)
                .retryCount(0)
                .build();

        message = messageRepository.save(message);

        MessengerDTO.MessageSendEvent event = MessengerDTO.MessageSendEvent.builder()
                .messageId(message.getId())
                .recipientId(message.getRecipientId())
                .content(message.getContent())
                .deepLink(message.getDeepLink())
                .fcmToken(fcmToken)
                .type(message.getType())
                .sentAt(message.getSentAt())
                .build();

        kafkaTemplate.send(messageSendTopic, message.getRecipientId(), event);

        log.info("Mensagem publicada no Kafka: messageId={}, recipientId={}",
                message.getId(), message.getRecipientId());

        return toResponse(message);
    }

    public MessengerDTO.MessageResponse updateStatus(String messageId,
                                                     Message.MessageStatus newStatus,
                                                     String failureReason) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Mensagem não encontrada: " + messageId));

        applyStatusTransition(message, newStatus, failureReason);
        message = messageRepository.save(message);

        MessengerDTO.MessageStatusEvent statusEvent = MessengerDTO.MessageStatusEvent.builder()
                .messageId(messageId)
                .recipientId(message.getRecipientId())
                .newStatus(newStatus)
                .failureReason(failureReason)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(messageStatusTopic, messageId, statusEvent);

        log.info("Status atualizado: messageId={}, status={}", messageId, newStatus);
        return toResponse(message);
    }

    public Page<MessengerDTO.MessageResponse> getMessagesByRecipient(String recipientId, Pageable pageable) {
        return messageRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable)
                .map(this::toResponse);
    }

    public MessengerDTO.MessageResponse findById(String id) {
        return toResponse(messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException("Mensagem não encontrada: " + id)));
    }

    private void applyStatusTransition(Message message, Message.MessageStatus newStatus, String failureReason) {
        message.setStatus(newStatus);
        switch (newStatus) {
            case DELIVERED -> message.setDeliveredAt(LocalDateTime.now());
            case READ      -> message.setReadAt(LocalDateTime.now());
            case FAILED    -> {
                message.setFailedAt(LocalDateTime.now());
                message.setFailureReason(failureReason);
            }
            default -> {}
        }
    }

    private void validateSendRequest(MessengerDTO.SendMessageRequest request) {
        boolean hasRecipient = request.getRecipientId() != null && !request.getRecipientId().isBlank();
        boolean hasSegment   = request.getSegmentId()   != null && !request.getSegmentId().isBlank();

        if (!hasRecipient && !hasSegment) {
            throw new IllegalArgumentException("Informe recipientId (1:1) ou segmentId (por segmento)");
        }
        if (hasRecipient && hasSegment) {
            throw new IllegalArgumentException("Informe apenas recipientId OU segmentId, não ambos");
        }
    }

    private MessengerDTO.MessageResponse toResponse(Message m) {
        return MessengerDTO.MessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSenderId())
                .recipientId(m.getRecipientId())
                .segmentId(m.getSegmentId())
                .campaignId(m.getCampaignId())
                .content(m.getContent())
                .deepLink(m.getDeepLink())
                .type(m.getType())
                .status(m.getStatus())
                .sentAt(m.getSentAt())
                .deliveredAt(m.getDeliveredAt())
                .readAt(m.getReadAt())
                .failedAt(m.getFailedAt())
                .failureReason(m.getFailureReason())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
