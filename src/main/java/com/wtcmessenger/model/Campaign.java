package com.wtcmessenger.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    private String id;

    private String title;

    private String content;

    private String deepLink;

    @Indexed
    private String segmentId;

    private List<String> recipientIds;

    @Indexed
    private String createdByOperatorId;

    @Indexed
    private CampaignStatus status;

    private LocalDateTime scheduledAt;
    private LocalDateTime dispatchedAt;

    private Integer totalRecipients;
    private Integer deliveredCount;
    private Integer readCount;
    private Integer failedCount;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum CampaignStatus {
        DRAFT,
        SCHEDULED,
        DISPATCHING,
        COMPLETED,
        FAILED
    }
}
