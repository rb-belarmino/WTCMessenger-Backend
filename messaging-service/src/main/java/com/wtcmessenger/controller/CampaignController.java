package com.wtcmessenger.controller;

import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.model.Campaign;
import com.wtcmessenger.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<MessengerDTO.ApiResponse<MessengerDTO.CampaignResponse>> createCampaign(
            @RequestBody Campaign campaign) {
        
        MessengerDTO.CampaignResponse response = campaignService.create(campaign);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessengerDTO.ApiResponse.success(response, "Rascunho de campanha criado com sucesso."));
    }

    @PostMapping("/{id}/schedule")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<MessengerDTO.ApiResponse<MessengerDTO.CampaignResponse>> scheduleCampaign(
            @PathVariable String id,
            @RequestBody MessengerDTO.ScheduleCampaignRequest request,
            Authentication authentication) {
        
        String operatorId = authentication != null ? authentication.getName() : "system";
        MessengerDTO.CampaignResponse response = campaignService.schedule(id, request, operatorId);
        
        return ResponseEntity.ok(MessengerDTO.ApiResponse.success(response, "Campanha agendada com sucesso."));
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<MessengerDTO.ApiResponse<List<MessengerDTO.CampaignResponse>>> getAllCampaigns() {
        return ResponseEntity.ok(MessengerDTO.ApiResponse.success(campaignService.findAll(), "Campanhas listadas."));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<MessengerDTO.ApiResponse<MessengerDTO.CampaignResponse>> getCampaignById(@PathVariable String id) {
        return ResponseEntity.ok(MessengerDTO.ApiResponse.success(campaignService.findById(id), "Campanha encontrada."));
    }
}