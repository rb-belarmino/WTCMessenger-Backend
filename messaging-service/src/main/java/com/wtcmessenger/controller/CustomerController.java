package com.wtcmessenger.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @GetMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<?> getAllCustomers() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", List.of()
        ));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<?> createCustomer(@RequestBody Map<String, Object> payload) {
        payload.put("id", UUID.randomUUID().toString());
        return ResponseEntity.status(201).body(Map.of(
            "success", true,
            "message", "Cliente criado com sucesso.",
            "data", payload
        ));
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<?> getCustomerTimeline(@PathVariable String id) {
        // Return 360 unified profile
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", Map.of(
                "customerId", id,
                "basicInfo", Map.of("name", "John Doe", "email", "john@wtcmessenger.com"),
                "recentMessages", List.of(),
                "campaignsReceived", List.of(),
                "openTasks", List.of()
            )
        ));
    }
}
