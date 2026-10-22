package com.openclassrooms.chatop.controller;

import com.openclassrooms.chatop.dto.request.MessageRequest;
import com.openclassrooms.chatop.dto.response.MessageResponse;
import com.openclassrooms.chatop.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages", description = "Envoi de messages à propos des locations")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @Operation(summary = "Envoyer un message")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Message envoyé",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
            @ApiResponse(responseCode = "403", description = "L'identifiant utilisateur ne correspond pas au JWT"),
            @ApiResponse(responseCode = "404", description = "Location ou utilisateur introuvable")
    })
    public ResponseEntity<MessageResponse> send(
            @Valid @RequestBody MessageRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(messageService.send(request, authentication.getName()));
    }
}
