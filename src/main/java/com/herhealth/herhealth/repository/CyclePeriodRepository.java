package com.herhealth.herhealth.repository;

import com.herhealth.herhealth.entity.CyclePeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CyclePeriodRepository extends JpaRepository<CyclePeriod, Long> {
    List<CyclePeriod> findAllByUserIdOrderByStartDateDesc(Long userId);
    Optional<CyclePeriod> findByIdAndUserId(Long id, Long userId);
    @Transactional
    void deleteAllByUserId(Long userId);
}