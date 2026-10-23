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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Gère les opérations métier de consultation et de gestion des locations. */
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

    /**
     * Récupère toutes les locations sous forme de réponse API.
     *
     * @return la liste des locations
     */
    @Transactional(readOnly = true)
    public RentalsResponse getAllRentals() {
        return new RentalsResponse(
                rentalRepository.findAll().stream()
                        .map(RentalResponse::from)
                        .toList()
        );
    }

    /**
     * Récupère le détail d'une location.
     *
     * @param id identifiant de la location
     * @return le détail de la location
     * @throws RentalNotFoundException si la location n'existe pas
     */
    @Transactional(readOnly = true)
    public RentalDetailResponse getRental(Integer id) {
        return rentalRepository.findById(id)
                .map(RentalDetailResponse::from)
                .orElseThrow(RentalNotFoundException::new);
    }

    /**
     * Crée une location appartenant à l'utilisateur authentifié.
     *
     * @param request données et image de la location
     * @param authenticatedEmail adresse e-mail de l'utilisateur authentifié
     * @throws UserNotFoundException si l'utilisateur authentifié n'existe pas
     */
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

    /**
     * Met à jour une location si elle appartient à l'utilisateur authentifié.
     *
     * @param id identifiant de la location à modifier
     * @param request nouvelles données de la location
     * @param authenticatedEmail adresse e-mail de l'utilisateur authentifié
     * @throws RentalNotFoundException si la location n'existe pas
     * @throws ForbiddenOperationException si la location appartient à un autre utilisateur
     */
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
        rental.setSurface(BigDecimal.valueOf(request.getSurface()));
        rental.setPrice(BigDecimal.valueOf(request.getPrice()));
        rental.setDescription(request.getDescription().trim());
    }
}
