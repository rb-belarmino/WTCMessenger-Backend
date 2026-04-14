package com.wtcmessenger.repository;

import com.wtcmessenger.model.Campaign;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends MongoRepository<Campaign, String> {

    List<Campaign> findByStatus(Campaign.CampaignStatus status);

    List<Campaign> findByStatusAndScheduledAtBefore(Campaign.CampaignStatus status, LocalDateTime now);

    List<Campaign> findBySegmentId(String segmentId);

    List<Campaign> findByCreatedByOperatorId(String operatorId);
}