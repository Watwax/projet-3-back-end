package com.openclassrooms.chatop.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openclassrooms.chatop.entity.Rental;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalDetailResponse(
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

    public static RentalDetailResponse from(Rental rental) {
        return new RentalDetailResponse(
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
