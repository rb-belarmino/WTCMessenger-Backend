package com.wtcmessenger.repository;

import com.wtcmessenger.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    Page<Message> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable pageable);

    List<Message> findBySenderIdAndRecipientIdOrderByCreatedAtAsc(String senderId, String recipientId);

    long countByRecipientIdAndStatus(String recipientId, Message.MessageStatus status);

    List<Message> findByCampaignId(String campaignId);

    long countByCampaignIdAndStatus(String campaignId, Message.MessageStatus status);

    List<Message> findByStatusAndRecipientId(Message.MessageStatus status, String recipientId);
}
