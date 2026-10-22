package com.openclassrooms.chatop.service;

import com.openclassrooms.chatop.dto.response.UserResponse;
import com.openclassrooms.chatop.exception.UserNotFoundException;
import com.openclassrooms.chatop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Integer id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(UserNotFoundException::new);
    }
}
