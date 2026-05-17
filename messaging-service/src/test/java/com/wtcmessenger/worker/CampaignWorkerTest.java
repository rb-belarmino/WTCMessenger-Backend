package com.wtcmessenger.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.firebase.FirebaseService;
import com.wtcmessenger.model.Message;
import com.wtcmessenger.repository.MessageRepository;
import com.wtcmessenger.service.CampaignService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignWorkerTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private FirebaseService firebaseService;

    @Mock
    private CampaignService campaignService;

    @Mock
    private Acknowledgment acknowledgment;

    private CampaignWorker campaignWorker;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        campaignWorker = new CampaignWorker(messageRepository, firebaseService, campaignService, objectMapper);
    }

    @Test
    void shouldProcessCampaignDispatchSuccessfully() throws Exception {
        // Arrange
        MessengerDTO.CampaignDispatchEvent event = MessengerDTO.CampaignDispatchEvent.builder()
                .campaignId("camp123")
                .recipientId("user456")
                .content("Special Promotion!")
                .deepLink("wtc://promo")
                .scheduledAt(LocalDateTime.now())
                .build();

        ConsumerRecord<String, Object> record = new ConsumerRecord<>("wtc.campaign.dispatch", 0, 0L, "key", event);

        Message savedMessage = Message.builder()
                .id("msg999")
                .senderId("system")
                .recipientId("user456")
                .campaignId("camp123")
                .content("Special Promotion!")
                .deepLink("wtc://promo")
                .type(Message.MessageType.CAMPAIGN)
                .status(Message.MessageStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();

        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // Act
        campaignWorker.processCampaignDispatch(record, acknowledgment);

        // Assert
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, times(2)).save(messageCaptor.capture());
        
        // Check first save (status SENT)
        Message firstSave = messageCaptor.getAllValues().get(0);
        assertEquals("system", firstSave.getSenderId());
        assertEquals("user456", firstSave.getRecipientId());
        assertEquals("camp123", firstSave.getCampaignId());
        assertEquals(Message.MessageStatus.SENT, firstSave.getStatus());

        // Verify Firebase SDK was called
        verify(firebaseService, times(1)).sendPushNotification(any(MessengerDTO.MessageSendEvent.class));

        // Verify Acknowledgment was committed (Kafka offset commit)
        verify(acknowledgment, times(1)).acknowledge();

        // Verify counters reconciliation
        verify(campaignService, times(1)).reconcileCampaignCounters("camp123");
    }

    @Test
    void shouldHandleExceptionAndStillAcknowledgeOffsetAndReconcileCounters() {
        // Arrange
        MessengerDTO.CampaignDispatchEvent event = MessengerDTO.CampaignDispatchEvent.builder()
                .campaignId("camp123")
                .recipientId("user456")
                .content("Special Promotion!")
                .build();

        ConsumerRecord<String, Object> record = new ConsumerRecord<>("wtc.campaign.dispatch", 0, 0L, "key", event);

        // Force an exception during database save
        when(messageRepository.save(any(Message.class))).thenThrow(new RuntimeException("DB Connection Timeout"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> campaignWorker.processCampaignDispatch(record, acknowledgment)
        );

        assertTrue(exception.getMessage().contains("DB Connection Timeout"));

        // Verify Acknowledgment and Reconcile are run in the finally block (guaranteed execution)
        verify(acknowledgment, times(1)).acknowledge();
        verify(campaignService, times(1)).reconcileCampaignCounters("camp123");
        verifyNoInteractions(firebaseService);
    }
}
