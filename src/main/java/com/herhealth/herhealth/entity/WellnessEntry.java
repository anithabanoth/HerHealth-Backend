package com.herhealth.herhealth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wellness_entries", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "entry_date" }))
public class WellnessEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    private String mood;

    @Column(precision = 4, scale = 1)
    private BigDecimal sleepHours;

    private Integer waterMl;

    private Integer exerciseMinutes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "wellness_entry_symptoms", joinColumns = @JoinColumn(name = "entry_id"))
    @Column(name = "symptom", nullable = false)
    private List<String> symptoms = new ArrayList<>();

    @Column(length = 1000)
    private String note;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public BigDecimal getSleepHours() { return sleepHours; }
    public void setSleepHours(BigDecimal sleepHours) { this.sleepHours = sleepHours; }
    public Integer getWaterMl() { return waterMl; }
    public void setWaterMl(Integer waterMl) { this.waterMl = waterMl; }
    public Integer getExerciseMinutes() { return exerciseMinutes; }
    public void setExerciseMinutes(Integer exerciseMinutes) { this.exerciseMinutes = exerciseMinutes; }
    public List<String> getSymptoms() { return symptoms; }
    public void setSymptoms(List<String> symptoms) { this.symptoms = symptoms; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}