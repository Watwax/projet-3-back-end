package com.openclassrooms.chatop.service;

import com.openclassrooms.chatop.dto.request.MessageRequest;
import com.openclassrooms.chatop.dto.response.MessageResponse;
import com.openclassrooms.chatop.entity.Message;
import com.openclassrooms.chatop.entity.Rental;
import com.openclassrooms.chatop.entity.User;
import com.openclassrooms.chatop.exception.ForbiddenOperationException;
import com.openclassrooms.chatop.exception.RentalNotFoundException;
import com.openclassrooms.chatop.exception.UserNotFoundException;
import com.openclassrooms.chatop.repository.MessageRepository;
import com.openclassrooms.chatop.repository.RentalRepository;
import com.openclassrooms.chatop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Gère l'envoi des messages associés aux locations. */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;

    public MessageService(
            MessageRepository messageRepository,
            RentalRepository rentalRepository,
            UserRepository userRepository
    ) {
        this.messageRepository = messageRepository;
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
    }

    /**
     * Enregistre un message après avoir vérifié l'identité de son auteur.
     *
     * @param request contenu du message et identifiants de l'utilisateur et de la location
     * @param authenticatedEmail adresse e-mail de l'utilisateur authentifié
     * @return une confirmation de l'envoi
     * @throws UserNotFoundException si l'utilisateur authentifié n'existe pas
     * @throws ForbiddenOperationException si l'identifiant fourni ne correspond pas à l'utilisateur authentifié
     * @throws RentalNotFoundException si la location n'existe pas
     */
    @Transactional
    public MessageResponse send(MessageRequest request, String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(UserNotFoundException::new);
        if (!user.getId().equals(request.userId())) {
            throw new ForbiddenOperationException();
        }

        Rental rental = rentalRepository.findById(request.rentalId())
                .orElseThrow(RentalNotFoundException::new);

        LocalDateTime now = LocalDateTime.now();
        Message message = new Message();
        message.setUser(user);
        message.setRental(rental);
        message.setMessage(request.message().trim());
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        messageRepository.save(message);

        return new MessageResponse("Message send with success");
    }
}
