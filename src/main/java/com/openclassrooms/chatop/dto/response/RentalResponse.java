package com.openclassrooms.chatop.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.openclassrooms.chatop.entity.Rental;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalResponse(
        Integer id,
        String name,
        Integer surface,
        Integer price,
        String picture,
        String description,
        @JsonProperty("owner_id") Integer ownerId,
        @JsonProperty("created_at") @JsonFormat(pattern = "yyyy/MM/dd") LocalDateTime createdAt,
        @JsonProperty("updated_at") @JsonFormat(pattern = "yyyy/MM/dd") LocalDateTime updatedAt
) {

    public static RentalResponse from(Rental rental) {
        return new RentalResponse(
                rental.getId(),
                rental.getName(),
                integerValue(rental.getSurface()),
                integerValue(rental.getPrice()),
                rental.getPicture(),
                rental.getDescription(),
                rental.getOwner().getId(),
                rental.getCreatedAt(),
                rental.getUpdatedAt()
        );
    }

    private static Integer integerValue(BigDecimal value) {
        return value == null ? null : value.intValueExact();
    }
}
