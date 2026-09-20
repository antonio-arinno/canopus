package com.arinno.canopus.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(statements = "INSERT INTO roles (id, name) VALUES (1, 'ROLE_USER'), (2, 'ROLE_ADMIN')",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AuthenticationAndTenantIsolationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registeredCompaniesCanAuthenticateAndOnlySeeTheirOwnUsers() throws Exception {
        registerCompany("Company A", "adminA", "adminA@example.com", "PasswordA1");
        registerCompany("Company B", "adminB", "adminB@example.com", "PasswordB1");

        String tokenA = login("adminA", "PasswordA1");
        String tokenB = login("adminB", "PasswordB1");

        createUser(tokenA, "memberA", "memberA@example.com");

        long adminAId = currentUserId(tokenA);
        long technologyId = createTechnology(tokenA, adminAId);
        long productId = createProduct(tokenA, technologyId, adminAId);
        long projectId = createProject(tokenA, productId, adminAId);
        long imputationId = createImputation(tokenA, projectId);
        updateImputation(tokenA, imputationId, projectId);

        mockMvc.perform(get("/user").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("adminA"))
            .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/user").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("adminB"))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/user/page?page=0&size=1").header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].countProducts").value(1))
            .andExpect(jsonPath("$.content[0].time").value(6))
            .andExpect(jsonPath("$.content[0].countProjects").doesNotExist())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.number").value(0));

        mockMvc.perform(get("/project/globalall").header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Integration project"))
            .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/project/globalall").header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/imputation")
                .header("Authorization", "Bearer " + tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ImputationPayload(projectId))))
            .andExpect(status().isNotFound())
            // CORRECCIÓN: Sincronizamos el texto exacto devuelto por tu orElseThrow() en el servicio
            .andExpect(jsonPath("$.message").value("Proyecto no encontrado para la empresa."));

    
    }

        private long currentUserId(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/user/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        }

        private long createTechnology(String token, long responsibleId) throws Exception {
        String body = objectMapper.writeValueAsString(new TechnologyPayload(responsibleId));
        mockMvc.perform(post("/technology")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/technology").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get(0).get("id").asLong();
        }

        private long createProduct(String token, long technologyId, long responsibleId) throws Exception {
        String body = objectMapper.writeValueAsString(new ProductPayload(technologyId, responsibleId));
        mockMvc.perform(post("/product")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/product").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get(0).get("id").asLong();
        }

        private long createProject(String token, long productId, long responsibleId) throws Exception {
        String body = objectMapper.writeValueAsString(new ProjectPayload(productId, responsibleId));
        mockMvc.perform(post("/project")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/project/globalall").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get(0).get("id").asLong();
        }

        private long createImputation(String token, long projectId) throws Exception {
        mockMvc.perform(post("/imputation")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new ImputationPayload(projectId))))
            .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/imputation/date/2026-09-02").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].project.id").value(projectId))
            .andExpect(jsonPath("$.items[0].time").value(8))
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        }

        private void updateImputation(String token, long imputationId, long projectId) throws Exception {
        mockMvc.perform(put("/imputation/{id}", imputationId)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new ImputationPayload(projectId, 6))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/imputation/date/2026-09-02").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].time").value(6));
        }

    private void registerCompany(String companyName, String username, String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new RegisterPayload(companyName, username, email, password));

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());
    }

    private void createUser(String token, String username, String email) throws Exception {
        String body = objectMapper.writeValueAsString(new UserPayload(username, email));

        mockMvc.perform(post("/user")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username));
    }

    private String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new LoginPayload(username, password));
        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = response.get("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    private record RegisterPayload(String companyName, String companyDescription, String name, String lastname,
            String email, String username, String password) {
        RegisterPayload(String companyName, String username, String email, String password) {
            this(companyName, "Integration test company", "Admin", "User", email, username, password);
        }
    }

    private record LoginPayload(String username, String password) {
    }

    private record UserPayload(String name, String lastname, String email, String username, boolean admin) {
        UserPayload(String username, String email) {
            this("Member", "User", email, username, false);
        }
    }

    private record TechnologyPayload(String name, String description, long responsibleId) {
        TechnologyPayload(long responsibleId) {
            this("Integration technology", "Integration test technology", responsibleId);
        }
    }

    private record ProductPayload(String name, String description, long technologyId, long responsibleId,
            long backupId) {
        ProductPayload(long technologyId, long responsibleId) {
            this("Integration product", "Integration test product", technologyId, responsibleId, responsibleId);
        }
    }

    private record ProjectPayload(String name, String description, String dateDev, Long productId, Long responsibleId,
            List<Long> contributorIds) {
        ProjectPayload(long productId, long responsibleId) {
            this("Integration project", "Integration test project", "2026-09-02", productId, responsibleId,
                    List.of());
        }
    }

    private record ImputationPayload(String date, List<ImputationItemPayload> items) {
        public ImputationPayload(long projectId) {
            this("2026-09-02", List.of(new ImputationItemPayload(projectId, 8)));
        }

        public ImputationPayload(long projectId, int time) {
            this("2026-09-02", List.of(new ImputationItemPayload(projectId, time)));
        }
    }

    private record ImputationItemPayload(long projectId, int time) {
    }
}
