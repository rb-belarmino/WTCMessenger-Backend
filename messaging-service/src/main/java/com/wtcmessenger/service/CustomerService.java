package com.wtcmessenger.service;

import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.model.Customer;
import com.wtcmessenger.model.Message;
import com.wtcmessenger.model.Campaign;
import com.wtcmessenger.repository.CustomerRepository;
import com.wtcmessenger.repository.MessageRepository;
import com.wtcmessenger.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final MessageRepository messageRepository;
    private final CampaignRepository campaignRepository;

    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findById(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + id));
    }

    public MessengerDTO.CustomerTimelineResponse getTimeline(String id) {
        Customer customer = findById(id);
        
        List<MessengerDTO.MessageResponse> recentMessages = messageRepository
                .findBySenderIdOrRecipientIdOrderByCreatedAtDesc(id, id, PageRequest.of(0, 50))
                .getContent()
                .stream()
                .map(this::toMessageResponse)
                .toList();
                
        List<MessengerDTO.CampaignResponse> activeCampaigns = campaignRepository
                .findAll()
                .stream()
                .filter(c -> c.getStatus() == Campaign.CampaignStatus.DISPATCHING || 
                             c.getStatus() == Campaign.CampaignStatus.COMPLETED)
                .map(this::toCampaignResponse)
                .toList();

        List<String> openTasks = List.of(
            "Retornar ligação de pós-venda para " + customer.getName(),
            "Confirmar recebimento da última campanha de engajamento",
            "Verificar score de engajamento (Score atual: " + String.format("%.1f", customer.getAdditionalAttributes() != null && customer.getAdditionalAttributes().containsKey("engagementScore") ? ((Number) customer.getAdditionalAttributes().get("engagementScore")).doubleValue() : 8.5) + ")"
        );

        return MessengerDTO.CustomerTimelineResponse.builder()
                .customer(customer)
                .recentMessages(recentMessages)
                .activeCampaigns(activeCampaigns)
                .openTasks(openTasks)
                .build();
    }

    private MessengerDTO.MessageResponse toMessageResponse(Message m) {
        return MessengerDTO.MessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSenderId())
                .recipientId(m.getRecipientId())
                .segmentId(m.getSegmentId())
                .campaignId(m.getCampaignId())
                .content(m.getContent())
                .deepLink(m.getDeepLink())
                .type(m.getType())
                .status(m.getStatus())
                .sentAt(m.getSentAt())
                .deliveredAt(m.getDeliveredAt())
                .readAt(m.getReadAt())
                .failedAt(m.getFailedAt())
                .failureReason(m.getFailureReason())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private MessengerDTO.CampaignResponse toCampaignResponse(Campaign c) {
        return MessengerDTO.CampaignResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .content(c.getContent())
                .deepLink(c.getDeepLink())
                .url(c.getUrl())
                .actions(c.getActions())
                .actionUrls(c.getActionUrls())
                .segmentId(c.getSegmentId())
                .status(c.getStatus() != null ? c.getStatus().name() : null)
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

