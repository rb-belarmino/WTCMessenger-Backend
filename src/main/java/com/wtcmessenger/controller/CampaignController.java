package com.wtcmessenger.controller;

import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.model.Campaign;
import com.wtcmessenger.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    public ResponseEntity<MessengerDTO.ApiResponse<MessengerDTO.CampaignResponse>> createCampaign(
            @RequestBody Campaign campaign,
            @RequestHeader(value = "X-Operator-Id", defaultValue = "system") String operatorId) {

        log.info("POST /campaigns - operatorId={}, title={}", operatorId, campaign.getTitle());

        campaign.setCreatedByOperatorId(operatorId);
        MessengerDTO.CampaignResponse response = campaignService.create(campaign);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MessengerDTO.ApiResponse.success(response, "Campanha criada com sucesso"));
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<MessengerDTO.ApiResponse<MessengerDTO.CampaignResponse>> scheduleCampaign(
            @PathVariable String id,
            @Valid @RequestBody MessengerDTO.ScheduleCampaignRequest request,
            @RequestHeader(value = "X-Operator-Id", defaultValue = "system") String operatorId) {

        log.info("POST /campaigns/{}/schedule - scheduledAt={}", id, request.getScheduledAt());

        MessengerDTO.CampaignResponse response = campaignService.schedule(id, request, operatorId);

        return ResponseEntity.ok(
                MessengerDTO.ApiResponse.success(response,
                        "Campanha agendada para " + request.getScheduledAt()));
    }

    @GetMapping
    public ResponseEntity<MessengerDTO.ApiResponse<List<MessengerDTO.CampaignResponse>>> listCampaigns() {
        return ResponseEntity.ok(
                MessengerDTO.ApiResponse.success(campaignService.findAll(), "Campanhas recuperadas"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessengerDTO.ApiResponse<MessengerDTO.CampaignResponse>> getCampaign(
            @PathVariable String id) {

        return ResponseEntity.ok(
                MessengerDTO.ApiResponse.success(campaignService.findById(id), "Campanha encontrada"));
    }
}