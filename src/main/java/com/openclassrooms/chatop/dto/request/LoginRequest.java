package com.openclassrooms.chatop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Schema(description = "Adresse e-mail", example = "user@example.com")
        @NotBlank @Email @Size(max = 255) String email,
        @Schema(description = "Mot de passe")
        @NotBlank @Size(min = 3, max = 72) String password
) {
}
