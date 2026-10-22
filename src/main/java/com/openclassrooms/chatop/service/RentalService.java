package com.openclassrooms.chatop.service;

import com.openclassrooms.chatop.dto.request.RentalRequest;
import com.openclassrooms.chatop.dto.response.RentalDetailResponse;
import com.openclassrooms.chatop.dto.response.RentalResponse;
import com.openclassrooms.chatop.dto.response.RentalsResponse;
import com.openclassrooms.chatop.entity.Rental;
import com.openclassrooms.chatop.entity.User;
import com.openclassrooms.chatop.exception.ForbiddenOperationException;
import com.openclassrooms.chatop.exception.RentalNotFoundException;
import com.openclassrooms.chatop.exception.UserNotFoundException;
import com.openclassrooms.chatop.repository.RentalRepository;
import com.openclassrooms.chatop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final RentalPictureStorage pictureStorage;

    public RentalService(
            RentalRepository rentalRepository,
            UserRepository userRepository,
            RentalPictureStorage pictureStorage
    ) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.pictureStorage = pictureStorage;
    }

    @Transactional(readOnly = true)
    public RentalsResponse getAllRentals() {
        return new RentalsResponse(
                rentalRepository.findAll().stream()
                        .map(RentalResponse::from)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public RentalDetailResponse getRental(Integer id) {
        return rentalRepository.findById(id)
                .map(RentalDetailResponse::from)
                .orElseThrow(RentalNotFoundException::new);
    }

    @Transactional
    public void createRental(RentalRequest request, String authenticatedEmail) {
        User owner = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(UserNotFoundException::new);

        Rental rental = new Rental();
        updateRentalFields(rental, request);
        rental.setPicture(pictureStorage.store(request.getPicture()));
        rental.setOwner(owner);

        LocalDateTime now = LocalDateTime.now();
        rental.setCreatedAt(now);
        rental.setUpdatedAt(now);
        rentalRepository.save(rental);
    }

    @Transactional
    public void updateRental(Integer id, RentalRequest request, String authenticatedEmail) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(RentalNotFoundException::new);
        if (!rental.getOwner().getEmail().equals(authenticatedEmail)) {
            throw new ForbiddenOperationException();
        }

        updateRentalFields(rental, request);
        if (request.getPicture() != null && !request.getPicture().isEmpty()) {
            rental.setPicture(pictureStorage.store(request.getPicture()));
        }
        rental.setUpdatedAt(LocalDateTime.now());
        rentalRepository.save(rental);
    }

    private void updateRentalFields(Rental rental, RentalRequest request) {
        rental.setName(request.getName().trim());
        rental.setSurface(request.getSurface());
        rental.setPrice(request.getPrice());
        rental.setDescription(request.getDescription().trim());
    }
}
