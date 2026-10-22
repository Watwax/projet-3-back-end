package com.openclassrooms.chatop.controller;

import com.openclassrooms.chatop.dto.request.RentalRequest;
import com.openclassrooms.chatop.dto.response.ApiMessage;
import com.openclassrooms.chatop.dto.response.RentalResponse;
import com.openclassrooms.chatop.dto.response.RentalsResponse;
import com.openclassrooms.chatop.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public ResponseEntity<RentalsResponse> getAllRentals() {
        return ResponseEntity.ok(rentalService.getAllRentals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalResponse> getRental(@PathVariable Integer id) {
        return ResponseEntity.ok(rentalService.getRental(id));
    }

    @PostMapping
    public ResponseEntity<ApiMessage> createRental(
            @Valid @ModelAttribute RentalRequest request,
            Authentication authentication
    ) {
        rentalService.createRental(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiMessage("Rental created !"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiMessage> updateRental(
            @PathVariable Integer id,
            @Valid @ModelAttribute RentalRequest request,
            Authentication authentication
    ) {
        rentalService.updateRental(id, request, authentication.getName());
        return ResponseEntity.ok(new ApiMessage("Rental updated !"));
    }
}
