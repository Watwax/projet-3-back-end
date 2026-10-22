package com.openclassrooms.chatop.service;

import com.openclassrooms.chatop.dto.response.RentalResponse;
import com.openclassrooms.chatop.dto.response.RentalsResponse;
import com.openclassrooms.chatop.repository.RentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;

    public RentalService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    @Transactional(readOnly = true)
    public RentalsResponse getAllRentals() {
        return new RentalsResponse(
                rentalRepository.findAll().stream()
                        .map(RentalResponse::from)
                        .toList()
        );
    }
}
