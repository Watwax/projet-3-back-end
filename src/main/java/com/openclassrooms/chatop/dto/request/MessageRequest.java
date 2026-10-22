package com.openclassrooms.chatop.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageRequest(
        @NotNull @JsonProperty("rental_id") Integer rentalId,
        @NotNull @JsonProperty("user_id") Integer userId,
        @NotBlank @Size(max = 2000) String message
) {
}
