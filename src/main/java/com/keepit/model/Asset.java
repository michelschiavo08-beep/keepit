package com.keepit.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String type; // Ex: IMPRESSORA, ROTEADOR, COMPUTADOR, SERVIDOR

    @Column(name = "acquisition_date", nullable = false)
    private LocalDate acquisitionDate;

    @Column(name = "usage_limit", nullable = false)
    private Long usageLimit; // Ex: 10000 paginas, 5000 horas

    @Column(name = "maintenance_interval_days", nullable = false)
    private Integer maintenanceIntervalDays; // Ex: 90, 180 dias

    @Column(name = "last_maintenance_date", nullable = false)
    private LocalDate lastMaintenanceDate;

    @Column(name = "current_usage", nullable = false)
    private Long currentUsage = 0L;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Asset() {
    }

    public Asset(String name, String type, LocalDate acquisitionDate, Long usageLimit, Integer maintenanceIntervalDays, LocalDate lastMaintenanceDate, Long currentUsage) {
        this.name = name;
        this.type = type;
        this.acquisitionDate = acquisitionDate;
        this.usageLimit = usageLimit;
        this.maintenanceIntervalDays = maintenanceIntervalDays;
        this.lastMaintenanceDate = lastMaintenanceDate != null ? lastMaintenanceDate : acquisitionDate;
        this.currentUsage = currentUsage != null ? currentUsage : 0L;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public Long getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Long usageLimit) {
        this.usageLimit = usageLimit;
    }

    public Integer getMaintenanceIntervalDays() {
        return maintenanceIntervalDays;
    }

    public void setMaintenanceIntervalDays(Integer maintenanceIntervalDays) {
        this.maintenanceIntervalDays = maintenanceIntervalDays;
    }

    public LocalDate getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public Long getCurrentUsage() {
        return currentUsage;
    }

    public void setCurrentUsage(Long currentUsage) {
        this.currentUsage = currentUsage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
