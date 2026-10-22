package com.openclassrooms.chatop.controller;

import com.openclassrooms.chatop.dto.request.MessageRequest;
import com.openclassrooms.chatop.dto.response.MessageResponse;
import com.openclassrooms.chatop.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> send(
            @Valid @RequestBody MessageRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(messageService.send(request, authentication.getName()));
    }
}
