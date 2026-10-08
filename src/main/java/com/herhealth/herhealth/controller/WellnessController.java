package com.herhealth.herhealth.controller;

import com.herhealth.herhealth.entity.User;
import com.herhealth.herhealth.entity.WellnessEntry;
import com.herhealth.herhealth.repository.UserRepository;
import com.herhealth.herhealth.repository.WellnessEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/wellness")
public class WellnessController {

    private static final Set<String> MOODS = Set.of("good", "okay", "low", "anxious", "irritable");
    private final WellnessEntryRepository entryRepository;
    private final UserRepository userRepository;

    public WellnessController(WellnessEntryRepository entryRepository, UserRepository userRepository) {
        this.entryRepository = entryRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<WellnessResponse> list(@AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        return entryRepository.findAllByUserIdOrderByEntryDateDesc(user.getId()).stream()
                .map(WellnessResponse::from)
                .toList();
    }

    @PutMapping
    public WellnessResponse save(@AuthenticationPrincipal UserDetails principal, @RequestBody WellnessRequest request) {
        validate(request);
        User user = currentUser(principal);
        WellnessEntry entry = entryRepository.findByUserIdAndEntryDate(user.getId(), request.entryDate())
                .orElseGet(WellnessEntry::new);
        entry.setUser(user);
        entry.setEntryDate(request.entryDate());
        entry.setMood(request.mood());
        entry.setSleepHours(request.sleepHours());
        entry.setWaterMl(request.waterMl());
        entry.setExerciseMinutes(request.exerciseMinutes());
        List<String> symptoms = request.symptoms() == null ? List.of() : request.symptoms().stream()
                .filter(symptom -> symptom != null && !symptom.isBlank())
                .distinct()
                .limit(12)
                .toList();
        entry.setSymptoms(new java.util.ArrayList<>(symptoms));
        entry.setNote(request.note() == null ? "" : request.note().trim());
        return WellnessResponse.from(entryRepository.save(entry));
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void validate(WellnessRequest request) {
        if (request.entryDate() == null || request.entryDate().isAfter(LocalDate.now())
                || request.mood() == null || !MOODS.contains(request.mood())
                || request.sleepHours() == null || request.sleepHours().compareTo(BigDecimal.ZERO) < 0
                || request.sleepHours().compareTo(new BigDecimal("24")) > 0
                || request.waterMl() == null || request.waterMl() < 0 || request.waterMl() > 10000
                || request.exerciseMinutes() == null || request.exerciseMinutes() < 0 || request.exerciseMinutes() > 1440
                || (request.note() != null && request.note().length() > 1000)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check the daily entry values and try again.");
        }
    }

    public record WellnessRequest(LocalDate entryDate, String mood, BigDecimal sleepHours, Integer waterMl,
            Integer exerciseMinutes, List<String> symptoms, String note) { }
    public record WellnessResponse(Long id, LocalDate entryDate, String mood, BigDecimal sleepHours, Integer waterMl,
            Integer exerciseMinutes, List<String> symptoms, String note) {
        static WellnessResponse from(WellnessEntry entry) {
            return new WellnessResponse(entry.getId(), entry.getEntryDate(), entry.getMood(), entry.getSleepHours(),
                    entry.getWaterMl(), entry.getExerciseMinutes(), entry.getSymptoms(), entry.getNote());
        }
    }
}