package com.herhealth.herhealth.repository;

import com.herhealth.herhealth.entity.WellnessEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WellnessEntryRepository extends JpaRepository<WellnessEntry, Long> {
    List<WellnessEntry> findAllByUserIdOrderByEntryDateDesc(Long userId);
    Optional<WellnessEntry> findByUserIdAndEntryDate(Long userId, LocalDate entryDate);
    @Transactional
    void deleteAllByUserId(Long userId);
}