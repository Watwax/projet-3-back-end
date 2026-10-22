package com.openclassrooms.chatop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.chatop.entity.Rental;
import com.openclassrooms.chatop.entity.User;
import com.openclassrooms.chatop.repository.MessageRepository;
import com.openclassrooms.chatop.repository.RentalRepository;
import com.openclassrooms.chatop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chatop-auth-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.upload.dir=target/test-uploads",
        "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.expiration-ms=3600000"
})
@AutoConfigureMockMvc
class AuthIntegrationTests {

    private static final String EMAIL = "auth-test@example.com";
    private static final String PASSWORD = "test!31";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearUsers() {
        messageRepository.deleteAll();
        rentalRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerPersistsAnEncodedPasswordAndReturnsTokenForCurrentUser() throws Exception {
        MvcResult registerResult = register(EMAIL, "Test User", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        User user = userRepository.findByEmail(EMAIL).orElseThrow();
        assertNotEquals(PASSWORD, user.getPassword());
        assertTrue(passwordEncoder.matches(PASSWORD, user.getPassword()));

        String token = tokenFrom(registerResult);
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginReturnsTokenForCorrectCredentialsAndUnauthorizedForIncorrectCredentials() throws Exception {
        register(EMAIL, "Test User", PASSWORD).andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"auth-test@example.com","password":"test!31"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + tokenFrom(loginResult)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"auth-test@example.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("error"));
    }

    @Test
    void registerRejectsInvalidAndDuplicateEmails() throws Exception {
        register("", "Test User", PASSWORD)
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{}"));

        register(EMAIL, "Test User", PASSWORD).andExpect(status().isOk());
        register(EMAIL, "Another User", PASSWORD)
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{}"));
    }

    @Test
    void allOtherRoutesRequireAValidBearerToken() throws Exception {
        mockMvc.perform(get("/api/rentals"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{}"));
    }

    @Test
    void getRentalsReturnsRentalDetailsForAuthenticatedUser() throws Exception {
        MvcResult registerResult = register(EMAIL, "Test User", PASSWORD)
                .andExpect(status().isOk())
                .andReturn();
        User owner = userRepository.findByEmail(EMAIL).orElseThrow();

        Rental rental = new Rental();
        rental.setName("Appartement lumineux");
        rental.setSurface(42);
        rental.setPrice(1200);
        rental.setPicture("https://example.com/rental.jpg");
        rental.setDescription("Un appartement de test");
        rental.setOwner(owner);
        rental.setCreatedAt(LocalDateTime.parse("2026-01-01T12:00:00"));
        rental.setUpdatedAt(LocalDateTime.parse("2026-01-02T12:00:00"));
        rentalRepository.save(rental);

        mockMvc.perform(get("/api/rentals")
                        .header("Authorization", "Bearer " + tokenFrom(registerResult)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentals.length()").value(1))
                .andExpect(jsonPath("$.rentals[0].id").value(rental.getId()))
                .andExpect(jsonPath("$.rentals[0].name").value("Appartement lumineux"))
                .andExpect(jsonPath("$.rentals[0].surface").value(42))
                .andExpect(jsonPath("$.rentals[0].price").value(1200))
                .andExpect(jsonPath("$.rentals[0].owner_id").value(owner.getId()))
                .andExpect(jsonPath("$.rentals[0].created_at").value("2026/01/01"))
                .andExpect(jsonPath("$.rentals[0].owner").doesNotExist());
    }

    @Test
    void rentalRoutesCreateReadAndUpdateRentalsForTheirOwner() throws Exception {
        MvcResult registerResult = register(EMAIL, "Test User", PASSWORD)
                .andExpect(status().isOk())
                .andReturn();
        String token = tokenFrom(registerResult);

        mockMvc.perform(multipart("/api/rentals")
                        .file(validPng())
                        .param("name", "Appartement test")
                        .param("surface", "42")
                        .param("price", "1200")
                        .param("description", "Description du logement")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Rental created !"))
                .andReturn();

        Rental rental = rentalRepository.findAll().get(0);
        mockMvc.perform(get("/api/rentals/{id}", rental.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rental.getId()))
                .andExpect(jsonPath("$.owner_id").value(userRepository.findByEmail(EMAIL).orElseThrow().getId()))
                .andExpect(jsonPath("$.picture").value(rental.getPicture()))
                .andExpect(jsonPath("$.created_at")
                        .value(rental.getCreatedAt().toLocalDate().toString().replace("-", "/")));

        mockMvc.perform(get(rental.getPicture()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG));

        mockMvc.perform(multipart("/api/rentals/{id}", rental.getId())
                        .param("name", "Appartement mis à jour")
                        .param("surface", "50")
                        .param("price", "1300")
                        .param("description", "Description mise à jour")
                        .header("Authorization", "Bearer " + token)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Rental updated !"));

        mockMvc.perform(get("/api/rentals/{id}", rental.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Appartement mis à jour"))
                .andExpect(jsonPath("$.picture").value(rental.getPicture()));
    }

    @Test
    void rentalUpdatesAndMessagesCannotBePerformedAsAnotherUser() throws Exception {
        MvcResult ownerRegisterResult = register(EMAIL, "Owner", PASSWORD)
                .andExpect(status().isOk())
                .andReturn();
        User owner = userRepository.findByEmail(EMAIL).orElseThrow();

        Rental rental = new Rental();
        rental.setName("Appartement test");
        rental.setSurface(42);
        rental.setPrice(1200);
        rental.setPicture("/uploads/test.png");
        rental.setDescription("Description");
        rental.setOwner(owner);
        rental.setCreatedAt(LocalDateTime.now());
        rental.setUpdatedAt(LocalDateTime.now());
        rentalRepository.save(rental);

        MvcResult otherRegisterResult = register("other@example.com", "Other", PASSWORD)
                .andExpect(status().isOk())
                .andReturn();
        String otherToken = tokenFrom(otherRegisterResult);
        Integer otherUserId = userRepository.findByEmail("other@example.com").orElseThrow().getId();

        mockMvc.perform(post("/api/messages")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rental_id":%d,"user_id":%d,"message":"A sufficiently long message"}
                                """.formatted(rental.getId(), owner.getId())))
                .andExpect(status().isForbidden());

        mockMvc.perform(multipart("/api/rentals/{id}", rental.getId())
                        .param("name", "Changed")
                        .param("surface", "42")
                        .param("price", "1200")
                        .param("description", "Description")
                        .header("Authorization", "Bearer " + otherToken)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/messages")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rental_id":%d,"user_id":%d,"message":"A sufficiently long message"}
                                """.formatted(rental.getId(), otherUserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message send with success"));

        mockMvc.perform(get("/api/user/{id}", owner.getId())
                        .header("Authorization", "Bearer " + tokenFrom(ownerRegisterResult)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void rentalCreationRejectsUnsupportedPictureTypes() throws Exception {
        String token = tokenFrom(register(EMAIL, "Test User", PASSWORD)
                .andExpect(status().isOk())
                .andReturn());

        mockMvc.perform(multipart("/api/rentals")
                        .file(new MockMultipartFile(
                                "picture",
                                "document.txt",
                                MediaType.TEXT_PLAIN_VALUE,
                                "not an image".getBytes()
                        ))
                        .param("name", "Appartement test")
                        .param("surface", "42")
                        .param("price", "1200")
                        .param("description", "Description du logement")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{}"));
        assertTrue(rentalRepository.findAll().isEmpty());
    }

    @Test
    void missingRentalsAndUsersReturnNotFoundAndNewRoutesRequireJwt() throws Exception {
        mockMvc.perform(get("/api/rentals/404"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/user/404"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rental_id":1,"user_id":1,"message":"A sufficiently long message"}
                                """))
                .andExpect(status().isUnauthorized());

        MvcResult registerResult = register(EMAIL, "Test User", PASSWORD)
                .andExpect(status().isOk())
                .andReturn();
        String token = tokenFrom(registerResult);
        mockMvc.perform(get("/api/rentals/404").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/user/404").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private ResultActions register(
            String email,
            String name,
            String password
    ) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterBody(email, name, password))));
    }

    private String tokenFrom(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private MockMultipartFile validPng() {
        return new MockMultipartFile(
                "picture",
                "picture.png",
                MediaType.IMAGE_PNG_VALUE,
                java.util.Base64.getDecoder().decode(
                        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/pS8AAAAASUVORK5CYII="
                )
        );
    }

    private record RegisterBody(String email, String name, String password) {
    }
}
