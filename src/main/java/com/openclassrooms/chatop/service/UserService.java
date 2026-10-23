package com.openclassrooms.chatop.service;

import com.openclassrooms.chatop.dto.response.UserResponse;
import com.openclassrooms.chatop.exception.UserNotFoundException;
import com.openclassrooms.chatop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Fournit les opérations de consultation des profils utilisateurs. */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Récupère le profil public d'un utilisateur.
     *
     * @param id identifiant de l'utilisateur
     * @return les informations publiques de l'utilisateur
     * @throws UserNotFoundException si aucun utilisateur ne correspond à cet identifiant
     */
    @Transactional(readOnly = true)
    public UserResponse getById(Integer id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(UserNotFoundException::new);
    }
}
