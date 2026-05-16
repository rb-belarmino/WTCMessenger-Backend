package com.wtcmessenger.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/campaigns")
public class CampaignController {

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<?> createCampaign(@RequestBody Map<String, Object> payload) {
        // Expected rich JSON for SwiftUI
        payload.put("id", UUID.randomUUID().toString());
        payload.put("status", "SCHEDULED");

        return ResponseEntity.status(201).body(Map.of(
            "success", true,
            "message", "Campanha agendada com sucesso.",
            "data", payload
        ));
    }
}