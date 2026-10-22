package com.example.basics;

import com.example.basics.model.Role;
import com.example.basics.model.User;
import com.example.basics.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests: the full application with the real security filter chain, real JWTs and an H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationIntegrationTest {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // A fresh random signing key per test run, held only in memory: no JWT secret is stored in the repository
    @DynamicPropertySource
    static void jwtSecret(DynamicPropertyRegistry registry) {
        byte[] key = new byte[64];
        RANDOM.nextBytes(key);
        registry.add("app.jwt.secret", () -> Base64.getEncoder().encodeToString(key));
    }

    @Test
    void registeredUserCanReadWithTokenButCannotWrite() throws Exception {
        String token = tokenFrom(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"reader\", \"email\": \"reader@example.com\", \"password\": \"secret123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(get("/api/v1/categories").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/categories").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Toys\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registeringSameUsernameTwiceReturns409() throws Exception {
        String body = "{\"username\": \"twice\", \"email\": \"twice@example.com\", \"password\": \"secret123\"}";
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        saveUser("locked", "right-password", Role.ROLE_USER);

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usernameOrEmail\": \"locked\", \"password\": \"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidOrMissingTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/items"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/items").header(HttpHeaders.AUTHORIZATION, "Bearer not.a.valid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void adminCanManageCategoriesAndItemsAgainstTheDatabase() throws Exception {
        String admin = bearer(loginAs("boss", Role.ROLE_ADMIN));

        String category = mockMvc.perform(post("/api/v1/categories").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Electronics\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long categoryId = objectMapper.readTree(category).get("id").asLong();

        String itemJson = "{\"name\": \"Laptop\", \"sku\": \"IT-001\", \"price\": 999.50, \"quantity\": 3, \"categoryId\": " + categoryId + "}";
        String item = mockMvc.perform(post("/api/v1/items").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(itemJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long itemId = objectMapper.readTree(item).get("id").asLong();

        mockMvc.perform(post("/api/v1/items").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(itemJson))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/items/category/" + categoryId).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("IT-001"));

        mockMvc.perform(get("/api/v1/categories/" + categoryId).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(1));

        mockMvc.perform(put("/api/v1/items/" + itemId).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(itemJson.replace("Laptop", "Gaming Laptop")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gaming Laptop"));

        mockMvc.perform(delete("/api/v1/items/" + itemId).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/items/" + itemId).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void sortingByUnknownFieldReturns400() throws Exception {
        String token = bearer(loginAs("sorter", Role.ROLE_USER));

        mockMvc.perform(get("/api/v1/items").param("sortBy", "doesNotExist").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid sort field 'doesNotExist'."));
    }

    @Test
    void unknownUrlReturns404Json() throws Exception {
        String token = bearer(loginAs("explorer", Role.ROLE_USER));

        mockMvc.perform(get("/api/v1/does-not-exist").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No endpoint found for '/api/v1/does-not-exist'."));
    }

    @Test
    void swaggerUiIsPublicAndLoadsTheCustomTheme() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/custom-swagger/swagger-theme.css")))
                .andExpect(content().string(containsString("/custom-swagger/theme-toggle.js")));

        mockMvc.perform(get("/custom-swagger/swagger-theme.css"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.BearerAuthentication.scheme").value("bearer"));
    }

    private void saveUser(String username, String password, Role role) {
        userRepository.save(new User(username, username + "@example.com", passwordEncoder.encode(password), Set.of(role)));
    }

    private String loginAs(String username, Role role) throws Exception {
        saveUser(username, "password-123", role);
        return tokenFrom(mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usernameOrEmail\": \"" + username + "\", \"password\": \"password-123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private String tokenFrom(String authResponseJson) throws Exception {
        JsonNode json = objectMapper.readTree(authResponseJson);
        return json.get("accessToken").asText();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
