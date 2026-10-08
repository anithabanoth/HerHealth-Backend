package com.herhealth.herhealth;

import com.herhealth.herhealth.entity.User;
import com.herhealth.herhealth.repository.CyclePeriodRepository;
import com.herhealth.herhealth.repository.UserRepository;
import com.herhealth.herhealth.repository.WellnessEntryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static com.jayway.jsonpath.JsonPath.read;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WellnessFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CyclePeriodRepository cycleRepository;

    @Autowired
    private WellnessEntryRepository wellnessRepository;

    private String testEmail;

    @Test
    void userCanRegisterLoginAndSavePrivateWellnessData() throws Exception {
        testEmail = "flow-" + UUID.randomUUID() + "@example.invalid";
        String password = "Test-password-1984";

        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"Test Member\",\"email\":\"" + testEmail + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("email", testEmail)
                        .param("password", password))
                .andExpect(status().isOk())
                .andReturn();
        String token = read(loginResult.getResponse().getContentAsString(), "$.token");
        String authorization = "Bearer " + token;

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail));

        LocalDate today = LocalDate.now();
        mockMvc.perform(post("/api/cycles")
                        .with(csrf())
                        .header("Authorization", authorization)
                        .contentType("application/json")
                        .content("{\"startDate\":\"" + today + "\",\"endDate\":\"" + today + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value(today.toString()));

        mockMvc.perform(put("/api/wellness")
                        .with(csrf())
                        .header("Authorization", authorization)
                        .contentType("application/json")
                        .content("{\"entryDate\":\"" + today + "\",\"mood\":\"good\",\"sleepHours\":7.5,\"waterMl\":1800,\"exerciseMinutes\":25,\"symptoms\":[\"Cramps\"],\"note\":\"Test entry\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symptoms[0]").value("Cramps"));

        mockMvc.perform(get("/api/cycles").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startDate").value(today.toString()));
        mockMvc.perform(get("/api/wellness").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mood").value("good"));
        mockMvc.perform(get("/api/cycles"))
                .andExpect(status().isUnauthorized());
    }

    @AfterEach
        @Transactional
    void removeTestAccount() {
        if (testEmail == null) return;
        userRepository.findByEmailIgnoreCase(testEmail).ifPresent(user -> {
            cycleRepository.deleteAllByUserId(user.getId());
            wellnessRepository.deleteAllByUserId(user.getId());
            userRepository.delete(user);
        });
    }
}