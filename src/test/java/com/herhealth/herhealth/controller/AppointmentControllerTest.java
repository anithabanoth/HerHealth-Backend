package com.herhealth.herhealth.controller;

import com.herhealth.herhealth.repository.AppointmentRepository;
import com.herhealth.herhealth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private String testEmail;

    @Test
    void authenticatedUserCanCreateAndListAppointments() throws Exception {
        testEmail = "appointments-test-" + UUID.randomUUID() + "@example.invalid";
        String password = "Test-password-1984";

        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"Appointment Tester\",\"email\":\"" + testEmail + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("email", testEmail)
                        .param("password", password))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        LocalDate date = LocalDate.of(2026, 10, 20);
        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .session(session)
                        .contentType("application/json")
                        .content("{\"title\":\"Consultation\",\"appointmentDate\":\"" + date + "\",\"appointmentTime\":\"14:00\",\"notes\":\"Bring reports\",\"status\":\"scheduled\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Consultation"));

        mockMvc.perform(get("/api/appointments").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Consultation"))
                .andExpect(jsonPath("$[0].status").value("scheduled"));
    }

    @AfterEach
    void removeTestData() {
        if (testEmail == null) {
            return;
        }
        userRepository.findByEmailIgnoreCase(testEmail).ifPresent(user -> {
            appointmentRepository.deleteAllByUserId(user.getId());
            userRepository.delete(user);
        });
    }
}
