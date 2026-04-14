package com.wtcmessenger.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "messages")
@CompoundIndex(name = "sender_recipient_idx", def = "{'senderId': 1, 'recipientId': 1}")
@CompoundIndex(name = "recipient_status_idx", def = "{'recipientId': 1, 'status': 1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    private String id;

    @Indexed
    private String senderId;

    @Indexed
    private String recipientId;

    private String segmentId;

    private String campaignId;

    private MessageType type;

    private String content;

    private String deepLink;

    private List<String> mediaUrls;

    @Indexed
    private MessageStatus status;

    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private LocalDateTime failedAt;

    private String failureReason;

    private Integer retryCount;

    private String fcmMessageId;
    private String fcmToken;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum MessageStatus {
        SENT,
        DELIVERED,
        READ,
        FAILED
    }

    public enum MessageType {
        CHAT,
        CAMPAIGN,
        NOTIFICATION
    }
}