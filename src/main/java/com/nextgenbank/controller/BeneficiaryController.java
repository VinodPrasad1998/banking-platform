package com.nextgenbank.controller;

import com.nextgenbank.dto.BeneficiaryRequest;
import com.nextgenbank.dto.BeneficiaryResponse;
import com.nextgenbank.dto.BeneficiaryStatusRequest;
import com.nextgenbank.service.BeneficiaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(
            BeneficiaryService beneficiaryService) {

        this.beneficiaryService = beneficiaryService;
    }

    @PostMapping
    public ResponseEntity<BeneficiaryResponse> createBeneficiary(
            @Valid @RequestBody BeneficiaryRequest request) {

        BeneficiaryResponse response =
                beneficiaryService.createBeneficiary(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{beneficiaryId}")
    public ResponseEntity<BeneficiaryResponse> getBeneficiaryById(
            @PathVariable Long beneficiaryId) {

        return ResponseEntity.ok(
                beneficiaryService.getBeneficiaryById(
                        beneficiaryId
                )
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BeneficiaryResponse>>
    getBeneficiariesByCustomer(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                beneficiaryService.getBeneficiariesByCustomer(
                        customerId
                )
        );
    }

    @PutMapping("/{beneficiaryId}/status")
    public ResponseEntity<BeneficiaryResponse>
    updateBeneficiaryStatus(
            @PathVariable Long beneficiaryId,
            @Valid @RequestBody BeneficiaryStatusRequest request) {

        return ResponseEntity.ok(
                beneficiaryService.updateBeneficiaryStatus(
                        beneficiaryId,
                        request
                )
        );
    }
}