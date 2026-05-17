package com.wtcmessenger.service;

import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.exception.InvalidCampaignException;
import com.wtcmessenger.model.Campaign;
import com.wtcmessenger.repository.CampaignRepository;
import com.wtcmessenger.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private CampaignService campaignService;

    private Campaign validCampaign;
    private Campaign invalidCampaign;

    @BeforeEach
    void setUp() {
        validCampaign = new Campaign();
        validCampaign.setId("1");
        validCampaign.setTitle("Winter Promo");
        validCampaign.setContent("Check out our new winter collection!");
        validCampaign.setUrl("https://example.com/banner.jpg");
        validCampaign.setActions(List.of("BUY_NOW", "LEARN_MORE"));
        validCampaign.setActionUrls(Map.of(
                "BUY_NOW", "wtcapp://store",
                "LEARN_MORE", "wtcapp://promo/winter"
        ));
        validCampaign.setStatus(Campaign.CampaignStatus.DRAFT);

        invalidCampaign = new Campaign();
        invalidCampaign.setTitle("No Action Promo");
        invalidCampaign.setContent("This has no actions.");
    }

    @Test
    void shouldCreateCampaignSuccessfullyWhenPayloadIsValid() {
        when(campaignRepository.save(any(Campaign.class))).thenReturn(validCampaign);

        MessengerDTO.CampaignResponse response = campaignService.create(validCampaign);

        assertNotNull(response);
        assertEquals("DRAFT", response.getStatus());
        verify(campaignRepository, times(1)).save(validCampaign);
    }

    @Test
    void shouldThrowInvalidCampaignExceptionWhenPayloadHasNoActions() {
        InvalidCampaignException exception = assertThrows(
                InvalidCampaignException.class,
                () -> campaignService.create(invalidCampaign)
        );

        assertEquals("A campanha deve conter pelo menos uma ação de engajamento com botões para o app SwiftUI.", exception.getMessage());
        verify(campaignRepository, never()).save(any(Campaign.class));
    }
}
