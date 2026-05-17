package com.wtcmessenger.service;

import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.exception.MessageNotFoundException;
import com.wtcmessenger.model.Message;
import com.wtcmessenger.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private MessageService messageService;

    private MessengerDTO.SendMessageRequest validRequest;
    private Message savedMessage;

    @BeforeEach
    void setUp() {
        validRequest = MessengerDTO.SendMessageRequest.builder()
                .recipientId("recipient123")
                .content("Hello World")
                .type(Message.MessageType.NOTIFICATION)
                .build();

        savedMessage = Message.builder()
                .id("msg123")
                .senderId("sender123")
                .recipientId("recipient123")
                .content("Hello World")
                .type(Message.MessageType.NOTIFICATION)
                .status(Message.MessageStatus.SENT)
                .build();
    }

    @Test
    void shouldSendMessageSuccessfullyWhenRequestIs1to1() {
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        MessengerDTO.MessageResponse response = messageService.sendMessage("sender123", validRequest, "fcmToken123");

        assertNotNull(response);
        assertEquals("msg123", response.getId());
        assertEquals(Message.MessageStatus.SENT, response.getStatus());

        verify(messageRepository, times(1)).save(any(Message.class));
        verify(kafkaTemplate, times(1)).send(any(), any(), any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenBothRecipientAndSegmentAreProvided() {
        MessengerDTO.SendMessageRequest invalidRequest = MessengerDTO.SendMessageRequest.builder()
                .recipientId("recipient123")
                .segmentId("segment123")
                .content("Hello")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageService.sendMessage("sender123", invalidRequest, null)
        );

        assertEquals("Informe apenas recipientId OU segmentId, não ambos", exception.getMessage());
        verifyNoInteractions(messageRepository, kafkaTemplate);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenNeitherRecipientNorSegmentAreProvided() {
        MessengerDTO.SendMessageRequest invalidRequest = MessengerDTO.SendMessageRequest.builder()
                .content("Hello")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageService.sendMessage("sender123", invalidRequest, null)
        );

        assertEquals("Informe recipientId (1:1) ou segmentId (por segmento)", exception.getMessage());
        verifyNoInteractions(messageRepository, kafkaTemplate);
    }

    @Test
    void shouldUpdateStatusSuccessfully() {
        when(messageRepository.findById("msg123")).thenReturn(Optional.of(savedMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        MessengerDTO.MessageResponse response = messageService.updateStatus("msg123", Message.MessageStatus.DELIVERED, null);

        assertNotNull(response);
        verify(messageRepository, times(1)).findById("msg123");
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(kafkaTemplate, times(1)).send(any(), any(), any());
    }

    @Test
    void shouldThrowMessageNotFoundExceptionWhenUpdatingStatusOfNonExistentMessage() {
        when(messageRepository.findById("nonexistent")).thenReturn(Optional.empty());

        MessageNotFoundException exception = assertThrows(
                MessageNotFoundException.class,
                () -> messageService.updateStatus("nonexistent", Message.MessageStatus.DELIVERED, null)
        );

        assertTrue(exception.getMessage().contains("Mensagem não encontrada"));
        verify(messageRepository, times(1)).findById("nonexistent");
        verify(messageRepository, never()).save(any(Message.class));
        verifyNoInteractions(kafkaTemplate);
    }
}
