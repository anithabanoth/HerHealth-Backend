package com.herhealth.herhealth.controller;

import com.herhealth.herhealth.entity.CyclePeriod;
import com.herhealth.herhealth.entity.User;
import com.herhealth.herhealth.repository.CyclePeriodRepository;
import com.herhealth.herhealth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cycles")
public class CycleController {

    private final CyclePeriodRepository cycleRepository;
    private final UserRepository userRepository;

    public CycleController(CyclePeriodRepository cycleRepository, UserRepository userRepository) {
        this.cycleRepository = cycleRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<CycleResponse> list(@AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        return cycleRepository.findAllByUserIdOrderByStartDateDesc(user.getId()).stream()
                .map(CycleResponse::from)
                .toList();
    }

    @PostMapping
    public CycleResponse create(@AuthenticationPrincipal UserDetails principal, @RequestBody CycleRequest request) {
        validate(request);
        CyclePeriod cycle = new CyclePeriod();
        cycle.setUser(currentUser(principal));
        cycle.setStartDate(request.startDate());
        cycle.setEndDate(request.endDate());
        return CycleResponse.from(cycleRepository.save(cycle));
    }

    @PutMapping("/{id}")
    public CycleResponse update(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id,
            @RequestBody CycleRequest request) {
        validate(request);
        User user = currentUser(principal);
        CyclePeriod cycle = cycleRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        cycle.setStartDate(request.startDate());
        cycle.setEndDate(request.endDate());
        return CycleResponse.from(cycleRepository.save(cycle));
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void validate(CycleRequest request) {
        if (request.startDate() == null || request.startDate().isAfter(LocalDate.now())
                || (request.endDate() != null && (request.endDate().isBefore(request.startDate())
                || request.endDate().isAfter(LocalDate.now())))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter valid period dates.");
        }
    }

    public record CycleRequest(LocalDate startDate, LocalDate endDate) { }
    public record CycleResponse(Long id, LocalDate startDate, LocalDate endDate) {
        static CycleResponse from(CyclePeriod period) {
            return new CycleResponse(period.getId(), period.getStartDate(), period.getEndDate());
        }
    }
}