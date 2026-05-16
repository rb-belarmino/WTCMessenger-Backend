package com.wtcmessenger.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/segments")
public class SegmentController {

    @GetMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<?> getAllSegments() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", List.of(
                Map.of("id", "1", "name", "Finance", "description", "Setor financeiro"),
                Map.of("id", "2", "name", "ESG", "description", "Responsabilidade ambiental")
            )
        ));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<?> createSegment(@RequestBody Map<String, Object> payload) {
        payload.put("id", UUID.randomUUID().toString());
        return ResponseEntity.status(201).body(Map.of(
            "success", true,
            "message", "Segmento corporativo criado com sucesso.",
            "data", payload
        ));
    }
}
