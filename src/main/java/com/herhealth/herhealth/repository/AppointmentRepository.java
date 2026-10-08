package com.herhealth.herhealth.repository;

import com.herhealth.herhealth.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByUserIdOrderByAppointmentDateAscAppointmentTimeAsc(Long userId);

    Optional<Appointment> findByIdAndUserId(Long id, Long userId);

    @Transactional
    void deleteAllByUserId(Long userId);
}
