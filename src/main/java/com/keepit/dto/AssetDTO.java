package com.keepit.dto;

import com.keepit.model.AssetStatus;
import java.time.LocalDate;
import java.util.List;

public class AssetDTO {

    private Long id;
    private String name;
    private String type;
    private LocalDate acquisitionDate;
    private Long usageLimit;
    private Integer maintenanceIntervalDays;
    private LocalDate lastMaintenanceDate;
    private Long currentUsage;
    
    // Campos Preditivos Calculados
    private AssetStatus status;
    private String statusLabel;
    private Double usagePercentage;
    private LocalDate nextMaintenanceDate;
    private Long daysUntilMaintenance;
    private List<String> alertReasons;

    public AssetDTO() {
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

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public Double getUsagePercentage() {
        return usagePercentage;
    }

    public void setUsagePercentage(Double usagePercentage) {
        this.usagePercentage = usagePercentage;
    }

    public LocalDate getNextMaintenanceDate() {
        return nextMaintenanceDate;
    }

    public void setNextMaintenanceDate(LocalDate nextMaintenanceDate) {
        this.nextMaintenanceDate = nextMaintenanceDate;
    }

    public Long getDaysUntilMaintenance() {
        return daysUntilMaintenance;
    }

    public void setDaysUntilMaintenance(Long daysUntilMaintenance) {
        this.daysUntilMaintenance = daysUntilMaintenance;
    }

    public List<String> getAlertReasons() {
        return alertReasons;
    }

    public void setAlertReasons(List<String> alertReasons) {
        this.alertReasons = alertReasons;
    }
}
