package com.wtcmessenger.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wtcmessenger.model.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class MessengerDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SendMessageRequest {
        private String recipientId;
        private String segmentId;

        @NotBlank(message = "O conteúdo da mensagem é obrigatório")
        private String content;

        private String deepLink;

        @NotNull(message = "O tipo de mensagem é obrigatório")
        private Message.MessageType type;

        private List<String> mediaUrls;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ScheduleCampaignRequest {

        @NotNull(message = "A data de agendamento é obrigatória")
        private LocalDateTime scheduledAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ABTestCampaignRequest {
        @NotBlank(message = "O título da variante A é obrigatório")
        private String variantATitle;

        @NotBlank(message = "O título da variante B é obrigatório")
        private String variantBTitle;

        @NotNull(message = "A porcentagem de split é obrigatória")
        private Integer splitPercentage;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateStatusRequest {

        @NotNull(message = "O status é obrigatório")
        private Message.MessageStatus status;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageSendEvent {
        private String messageId;
        private String recipientId;
        private String content;
        private String deepLink;
        private String fcmToken;
        private Message.MessageType type;
        private String campaignId;
        private LocalDateTime sentAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageStatusEvent {
        private String messageId;
        private String recipientId;
        private Message.MessageStatus newStatus;
        private String failureReason;
        private LocalDateTime timestamp;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CampaignDispatchEvent {
        private String campaignId;
        private String recipientId;
        private String content;
        private String deepLink;
        private LocalDateTime scheduledAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MessageResponse {
        private String id;
        private String senderId;
        private String recipientId;
        private String segmentId;
        private String campaignId;
        private String content;
        private String deepLink;
        private Message.MessageType type;
        private Message.MessageStatus status;
        private LocalDateTime sentAt;
        private LocalDateTime deliveredAt;
        private LocalDateTime readAt;
        private LocalDateTime failedAt;
        private String failureReason;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CampaignResponse {
        private String id;
        private String title;
        private String content;
        private String deepLink;
        private String url;
        private List<String> actions;
        private java.util.Map<String, String> actionUrls;
        private String segmentId;
        private String status;
        private LocalDateTime scheduledAt;
        private LocalDateTime dispatchedAt;
        private Integer totalRecipients;
        private Integer deliveredCount;
        private Integer readCount;
        private Integer failedCount;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CustomerTimelineResponse {
        private com.wtcmessenger.model.Customer customer;
        private List<MessengerDTO.MessageResponse> recentMessages;
        private List<MessengerDTO.CampaignResponse> activeCampaigns;
        private List<String> openTasks;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp;

        public static <T> ApiResponse<T> success(T data, String message) {
            return ApiResponse.<T>builder()
                    .success(true).message(message).data(data)
                    .timestamp(LocalDateTime.now()).build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                    .success(false).message(message)
                    .timestamp(LocalDateTime.now()).build();
        }
    }
}

