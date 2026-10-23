package com.openclassrooms.chatop.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageRequest(
        @Schema(description = "Identifiant de la location", example = "1")
        @NotNull @JsonProperty("rental_id") Integer rentalId,
        @Schema(description = "Identifiant de l'utilisateur authentifié", example = "1")
        @NotNull @JsonProperty("user_id") Integer userId,
        @Schema(description = "Contenu du message", example = "Bonjour, cette location est-elle disponible ?")
        @NotBlank @Size(max = 2000) String message
) {
}
