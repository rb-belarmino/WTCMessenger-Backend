package com.wtcmessenger.controller;

import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessengerDTO.ApiResponse<MessengerDTO.MessageResponse>> sendMessage(
            @Valid @RequestBody MessengerDTO.SendMessageRequest request,
            @RequestHeader(value = "X-Operator-Id", defaultValue = "system") String operatorId,
            @RequestHeader(value = "X-FCM-Token", required = false) String fcmToken) {

        log.info("POST /messages - operatorId={}, type={}", operatorId, request.getType());

        MessengerDTO.MessageResponse response = messageService.sendMessage(operatorId, request, fcmToken);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MessengerDTO.ApiResponse.success(response,
                        "Mensagem enviada ao Kafka. Entrega via Firebase em andamento."));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<MessengerDTO.ApiResponse<MessengerDTO.MessageResponse>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody MessengerDTO.UpdateStatusRequest request) {

        log.info("PUT /messages/{}/status - newStatus={}", id, request.getStatus());

        MessengerDTO.MessageResponse response = messageService.updateStatus(
                id, request.getStatus(), null);

        return ResponseEntity.ok(
                MessengerDTO.ApiResponse.success(response, "Status atualizado para " + request.getStatus()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessengerDTO.ApiResponse<MessengerDTO.MessageResponse>> getMessage(
            @PathVariable String id) {

        return ResponseEntity.ok(
                MessengerDTO.ApiResponse.success(messageService.findById(id), "Mensagem encontrada"));
    }

    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<MessengerDTO.ApiResponse<Page<MessengerDTO.MessageResponse>>> getMessagesByRecipient(
            @PathVariable String recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<MessengerDTO.MessageResponse> messages = messageService
                .getMessagesByRecipient(recipientId, PageRequest.of(page, size));

        return ResponseEntity.ok(
                MessengerDTO.ApiResponse.success(messages, "Histórico de mensagens do cliente"));
    }
}
