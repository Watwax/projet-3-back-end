package com.openclassrooms.chatop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public class RentalRequest {

    @Schema(description = "Nom de la location", example = "Appartement lumineux")
    @NotBlank
    @Size(max = 255)
    private String name;

    @Schema(description = "Surface en mètres carrés", example = "42")
    @NotNull
    @Positive
    private Integer surface;

    @Schema(description = "Prix par nuit", example = "120")
    @NotNull
    @PositiveOrZero
    private Integer price;

    @Schema(description = "Fichier image JPEG, PNG ou GIF", type = "string", format = "binary")
    private MultipartFile picture;

    @Schema(description = "Description de la location", example = "Appartement proche du centre")
    @NotBlank
    @Size(max = 2000)
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSurface() {
        return surface;
    }

    public void setSurface(Integer surface) {
        this.surface = surface;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public MultipartFile getPicture() {
        return picture;
    }

    public void setPicture(MultipartFile picture) {
        this.picture = picture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
