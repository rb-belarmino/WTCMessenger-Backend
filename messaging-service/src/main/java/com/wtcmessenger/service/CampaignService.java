package com.wtcmessenger.service;

import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.exception.InvalidCampaignException;
import com.wtcmessenger.exception.MessageNotFoundException;
import com.wtcmessenger.model.Campaign;
import com.wtcmessenger.model.Message;
import com.wtcmessenger.repository.CampaignRepository;
import com.wtcmessenger.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final MessageRepository messageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.campaign-dispatch}")
    private String campaignDispatchTopic;

    public MessengerDTO.CampaignResponse create(Campaign campaign) {
        if (campaign.getActions() == null || campaign.getActions().isEmpty()) {
            throw new InvalidCampaignException("A campanha deve conter pelo menos uma ação de engajamento com botões para o app SwiftUI.");
        }
        campaign.setStatus(Campaign.CampaignStatus.DRAFT);
        campaign.setDeliveredCount(0);
        campaign.setReadCount(0);
        campaign.setFailedCount(0);
        return toResponse(campaignRepository.save(campaign));
    }

    public MessengerDTO.CampaignResponse schedule(String campaignId,
                                                  MessengerDTO.ScheduleCampaignRequest request,
                                                  String operatorId) {
        Campaign campaign = findOrThrow(campaignId);

        if (campaign.getStatus() != Campaign.CampaignStatus.DRAFT) {
            throw new IllegalStateException(
                    "Apenas campanhas em DRAFT podem ser agendadas. Status atual: " + campaign.getStatus());
        }

        campaign.setScheduledAt(request.getScheduledAt());
        campaign.setStatus(Campaign.CampaignStatus.SCHEDULED);
        campaign = campaignRepository.save(campaign);

        log.info("Campanha agendada: campaignId={}, scheduledAt={}", campaignId, request.getScheduledAt());
        return toResponse(campaign);
    }

    @Scheduled(fixedDelay = 60_000)
    public void dispatchScheduledCampaigns() {
        List<Campaign> ready = campaignRepository.findByStatusAndScheduledAtBefore(
                Campaign.CampaignStatus.SCHEDULED, LocalDateTime.now());

        if (ready.isEmpty()) return;

        log.info("{} campanha(s) pronta(s) para disparo", ready.size());

        for (Campaign campaign : ready) {
            try {
                dispatchCampaign(campaign);
            } catch (Exception e) {
                log.error("Falha ao disparar campaignId={}: {}", campaign.getId(), e.getMessage());
                campaign.setStatus(Campaign.CampaignStatus.FAILED);
                campaignRepository.save(campaign);
            }
        }
    }

    private void dispatchCampaign(Campaign campaign) {
        campaign.setStatus(Campaign.CampaignStatus.DISPATCHING);
        campaign.setDispatchedAt(LocalDateTime.now());
        campaignRepository.save(campaign);

        List<String> recipients = campaign.getRecipientIds();

        if (recipients == null || recipients.isEmpty()) {
            log.warn("Campanha {} sem destinatários.", campaign.getId());
            campaign.setStatus(Campaign.CampaignStatus.COMPLETED);
            campaignRepository.save(campaign);
            return;
        }

        campaign.setTotalRecipients(recipients.size());
        campaignRepository.save(campaign);

        for (String recipientId : recipients) {
            MessengerDTO.CampaignDispatchEvent event = MessengerDTO.CampaignDispatchEvent.builder()
                    .campaignId(campaign.getId())
                    .recipientId(recipientId)
                    .content(campaign.getContent())
                    .deepLink(campaign.getDeepLink())
                    .scheduledAt(campaign.getScheduledAt())
                    .build();

            kafkaTemplate.send(campaignDispatchTopic, recipientId, event);
        }

        log.info("Campanha {} disparada para {} destinatários", campaign.getId(), recipients.size());
    }

    public MessengerDTO.CampaignResponse findById(String id) {
        return toResponse(findOrThrow(id));
    }

    public List<MessengerDTO.CampaignResponse> findAll() {
        return campaignRepository.findAll().stream().map(this::toResponse).toList();
    }

    public void reconcileCampaignCounters(String campaignId) {
        Campaign campaign = findOrThrow(campaignId);

        long delivered = messageRepository.countByCampaignIdAndStatus(campaignId, Message.MessageStatus.DELIVERED);
        long read      = messageRepository.countByCampaignIdAndStatus(campaignId, Message.MessageStatus.READ);
        long failed    = messageRepository.countByCampaignIdAndStatus(campaignId, Message.MessageStatus.FAILED);
        long sent      = messageRepository.countByCampaignIdAndStatus(campaignId, Message.MessageStatus.SENT);

        campaign.setDeliveredCount((int) delivered);
        campaign.setReadCount((int) read);
        campaign.setFailedCount((int) failed);

        if (sent == 0 && campaign.getStatus() == Campaign.CampaignStatus.DISPATCHING) {
            campaign.setStatus(Campaign.CampaignStatus.COMPLETED);
            log.info("Campanha {} concluída: delivered={}, read={}, failed={}",
                    campaignId, delivered, read, failed);
        }

        campaignRepository.save(campaign);
    }

    private Campaign findOrThrow(String id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException("Campanha não encontrada: " + id));
    }

    private MessengerDTO.CampaignResponse toResponse(Campaign c) {
        return MessengerDTO.CampaignResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .content(c.getContent())
                .deepLink(c.getDeepLink())
                .segmentId(c.getSegmentId())
                .status(c.getStatus().name())
                .scheduledAt(c.getScheduledAt())
                .dispatchedAt(c.getDispatchedAt())
                .totalRecipients(c.getTotalRecipients())
                .deliveredCount(c.getDeliveredCount())
                .readCount(c.getReadCount())
                .failedCount(c.getFailedCount())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
