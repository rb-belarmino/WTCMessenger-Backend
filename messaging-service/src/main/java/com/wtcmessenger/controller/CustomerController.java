package com.wtcmessenger.controller;

import com.wtcmessenger.dto.MessengerDTO;
import com.wtcmessenger.model.Customer;
import com.wtcmessenger.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<MessengerDTO.ApiResponse<List<Customer>>> getAllCustomers() {
        return ResponseEntity.ok(MessengerDTO.ApiResponse.success(customerService.findAll(), "Clientes listados com sucesso."));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<MessengerDTO.ApiResponse<Customer>> createCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerService.create(customer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessengerDTO.ApiResponse.success(savedCustomer, "Cliente criado com sucesso."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<MessengerDTO.ApiResponse<Customer>> getCustomerById(@PathVariable String id) {
        return ResponseEntity.ok(MessengerDTO.ApiResponse.success(customerService.findById(id), "Cliente encontrado."));
    }
}
