package com.openclassrooms.chatop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.chatop.entity.Rental;
import com.openclassrooms.chatop.entity.User;
import com.openclassrooms.chatop.repository.RentalRepository;
import com.openclassrooms.chatop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chatop-auth-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
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
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearUsers() {
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
                .andExpect(jsonPath("$.message").value("error"));

        register(EMAIL, "Test User", PASSWORD).andExpect(status().isOk());
        register(EMAIL, "Another User", PASSWORD)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("error"));
    }

    @Test
    void allOtherRoutesRequireAValidBearerToken() throws Exception {
        mockMvc.perform(get("/api/rentals"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("error"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("error"));
    }

    @Test
    void getRentalsReturnsRentalDetailsForAuthenticatedUser() throws Exception {
        MvcResult registerResult = register(EMAIL, "Test User", PASSWORD)
                .andExpect(status().isOk())
                .andReturn();
        User owner = userRepository.findByEmail(EMAIL).orElseThrow();

        Rental rental = new Rental();
        rental.setName("Appartement lumineux");
        rental.setSurface(new BigDecimal("42.50"));
        rental.setPrice(new BigDecimal("1200.00"));
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
                .andExpect(jsonPath("$.rentals[0].surface").value(42.5))
                .andExpect(jsonPath("$.rentals[0].price").value(1200.0))
                .andExpect(jsonPath("$.rentals[0].owner_id").value(owner.getId()))
                .andExpect(jsonPath("$.rentals[0].created_at").value("2026-01-01T12:00:00"))
                .andExpect(jsonPath("$.rentals[0].owner").doesNotExist());
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

    private record RegisterBody(String email, String name, String password) {
    }
}
