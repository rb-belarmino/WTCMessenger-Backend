package com.wtcmessenger.controller;

import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<MessengerDTO.ApiResponse<MessengerDTO.MessageResponse>> sendMessage(
            @RequestBody MessengerDTO.SendMessageRequest request,
            @RequestHeader(value = "X-FCM-Token", required = false) String fcmToken,
            Authentication authentication) {

        String senderId = authentication != null ? authentication.getName() : "system";
        MessengerDTO.MessageResponse response = messageService.sendMessage(senderId, request, fcmToken);

        return ResponseEntity.ok(MessengerDTO.ApiResponse.success(response, "Mensagem enviada com sucesso para processamento."));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<MessengerDTO.ApiResponse<MessengerDTO.MessageResponse>> getMessageById(@PathVariable String id) {
        return ResponseEntity.ok(MessengerDTO.ApiResponse.success(messageService.findById(id), "Mensagem encontrada."));
    }
}
