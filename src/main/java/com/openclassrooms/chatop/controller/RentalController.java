package com.openclassrooms.chatop.controller;

import com.openclassrooms.chatop.dto.request.RentalRequest;
import com.openclassrooms.chatop.dto.response.ApiMessage;
import com.openclassrooms.chatop.dto.response.RentalDetailResponse;
import com.openclassrooms.chatop.dto.response.RentalsResponse;
import com.openclassrooms.chatop.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rentals")
@Tag(name = "Locations", description = "Consultation et gestion des locations")
@SecurityRequirement(name = "bearerAuth")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    @Operation(summary = "Lister les locations")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des locations",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RentalsResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
    })
    public ResponseEntity<RentalsResponse> getAllRentals() {
        return ResponseEntity.ok(rentalService.getAllRentals());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une location")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Détail de la location",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RentalDetailResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
            @ApiResponse(responseCode = "404", description = "Location introuvable")
    })
    public ResponseEntity<RentalDetailResponse> getRental(
            @Parameter(description = "Identifiant de la location", example = "1")
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(rentalService.getRental(id));
    }

    @PostMapping
    @Operation(
            summary = "Créer une location",
            description = "Envoie les données et l'image sous forme multipart/form-data.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = RentalRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Location créée",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiMessage.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Données ou image invalides"),
            @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
    })
    public ResponseEntity<ApiMessage> createRental(
            @Valid @ModelAttribute RentalRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        rentalService.createRental(request, authentication.getName());
        return ResponseEntity.ok(new ApiMessage("Rental created !"));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Modifier une location",
            description = "Met à jour les données et accepte une nouvelle image facultative.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = RentalRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Location mise à jour",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiMessage.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
            @ApiResponse(responseCode = "403", description = "La location appartient à un autre utilisateur"),
            @ApiResponse(responseCode = "404", description = "Location introuvable")
    })
    public ResponseEntity<ApiMessage> updateRental(
            @Parameter(description = "Identifiant de la location", example = "1")
            @PathVariable Integer id,
            @Valid @ModelAttribute RentalRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        rentalService.updateRental(id, request, authentication.getName());
        return ResponseEntity.ok(new ApiMessage("Rental updated !"));
    }
}
