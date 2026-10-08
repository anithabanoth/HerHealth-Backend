package com.herhealth.herhealth.controller;

import com.herhealth.herhealth.entity.Appointment;
import com.herhealth.herhealth.entity.User;
import com.herhealth.herhealth.repository.AppointmentRepository;
import com.herhealth.herhealth.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public AppointmentController(AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<AppointmentResponse> list(@AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        return appointmentRepository.findAllByUserIdOrderByAppointmentDateAscAppointmentTimeAsc(user.getId()).stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AppointmentRequest request) {
        User user = currentUser(principal);
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setTitle(request.title());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setAppointmentTime(request.appointmentTime());
        appointment.setNotes(request.notes());
        appointment.setStatus(request.status() == null ? "scheduled" : request.status());
        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.from(saved));
    }

    @PutMapping("/{id}")
    public AppointmentResponse update(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {
        User user = currentUser(principal);
        Appointment appointment = appointmentRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found."));
        appointment.setTitle(request.title());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setAppointmentTime(request.appointmentTime());
        appointment.setNotes(request.notes());
        appointment.setStatus(request.status() == null ? "scheduled" : request.status());
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        User user = currentUser(principal);
        Appointment appointment = appointmentRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found."));
        appointmentRepository.delete(appointment);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public record AppointmentRequest(
            @NotBlank(message = "Appointment title is required") String title,
            @NotNull(message = "Appointment date is required") LocalDate appointmentDate,
            @NotNull(message = "Appointment time is required") LocalTime appointmentTime,
            String notes,
            String status) {
    }

    public record AppointmentResponse(Long id, String title, LocalDate appointmentDate, LocalTime appointmentTime,
            String notes, String status) {
        static AppointmentResponse from(Appointment appointment) {
            return new AppointmentResponse(appointment.getId(), appointment.getTitle(), appointment.getAppointmentDate(),
                    appointment.getAppointmentTime(), appointment.getNotes(), appointment.getStatus());
        }
    }
}
