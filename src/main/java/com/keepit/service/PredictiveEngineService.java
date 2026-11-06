package com.keepit.service;

import com.keepit.dto.AssetDTO;
import com.keepit.model.Asset;
import com.keepit.model.AssetStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Motor de Alertas Preditivos (Predictive Engine).
 * 
 * Regra de Negócio:
 * - Cruza o volume de uso atual com o limite estabelecido.
 * - Calcula a próxima data de manutenção com base na última manutenção + intervalo em dias.
 * - Dispara alerta "Manutenção Necessária" se:
 *   1. O volume de uso atingir ou ultrapassar 85% do limite de ciclo (ex: 8.500 de 10.000 páginas);
 *   2. Faltarem 15 dias ou menos para a data de manutenção programada (ou se a data já expirou).
 */
@Service
public class PredictiveEngineService {

    // Limiar percentual de alerta preditivo (85%)
    private static final double USAGE_THRESHOLD_PERCENTAGE = 0.85;
    
    // Limiar de dias restantes para manutenção preventiva (15 dias)
    private static final long DAYS_THRESHOLD = 15L;

    /**
     * Processa um Ativo e calcula seu DTO enriquecido com o diagnóstico preditivo.
     */
    public AssetDTO processAssetPredictiveStatus(Asset asset) {
        AssetDTO dto = new AssetDTO();
        dto.setId(asset.getId());
        dto.setName(asset.getName());
        dto.setType(asset.getType());
        dto.setAcquisitionDate(asset.getAcquisitionDate());
        dto.setUsageLimit(asset.getUsageLimit());
        dto.setMaintenanceIntervalDays(asset.getMaintenanceIntervalDays());
        dto.setLastMaintenanceDate(asset.getLastMaintenanceDate());
        dto.setCurrentUsage(asset.getCurrentUsage());

        // 1. Cálculo da Porcentagem do Limite de Ciclo de Uso
        double usageRatio = asset.getUsageLimit() > 0 
                ? (double) asset.getCurrentUsage() / asset.getUsageLimit() 
                : 0.0;
        double usagePercentage = Math.round(usageRatio * 1000.0) / 10.0; // Ex: 87.0%
        dto.setUsagePercentage(usagePercentage);

        // 2. Cálculo da Data Programada da Próxima Manutenção e Dias Restantes
        LocalDate baseDate = asset.getLastMaintenanceDate() != null 
                ? asset.getLastMaintenanceDate() 
                : asset.getAcquisitionDate();
        
        LocalDate nextMaintenanceDate = baseDate.plusDays(asset.getMaintenanceIntervalDays());
        long daysUntilMaintenance = ChronoUnit.DAYS.between(LocalDate.now(), nextMaintenanceDate);
        
        dto.setNextMaintenanceDate(nextMaintenanceDate);
        dto.setDaysUntilMaintenance(daysUntilMaintenance);

        // 3. Avaliação dos Critérios de Alerta Preditivo
        List<String> alertReasons = new ArrayList<>();
        
        boolean usageAlertTriggered = usageRatio >= USAGE_THRESHOLD_PERCENTAGE;
        boolean dateAlertTriggered = daysUntilMaintenance <= DAYS_THRESHOLD;

        if (usageAlertTriggered) {
            alertReasons.add(String.format("Volume de uso atingiu %.1f%% do limite (%d de %d)", 
                    usagePercentage, asset.getCurrentUsage(), asset.getUsageLimit()));
        }

        if (dateAlertTriggered) {
            if (daysUntilMaintenance < 0) {
                alertReasons.add(String.format("Manutenção VENCIDA há %d dia(s) (programada para %s)", 
                        Math.abs(daysUntilMaintenance), nextMaintenanceDate));
            } else {
                alertReasons.add(String.format("Faltam apenas %d dia(s) para a manutenção preventiva (%s)", 
                        daysUntilMaintenance, nextMaintenanceDate));
            }
        }

        // Definir status final baseado nos alertas ativados
        if (usageAlertTriggered || dateAlertTriggered) {
            dto.setStatus(AssetStatus.ALERTA_MANUTENCAO);
            dto.setStatusLabel(AssetStatus.ALERTA_MANUTENCAO.getLabel());
        } else {
            dto.setStatus(AssetStatus.OK);
            dto.setStatusLabel(AssetStatus.OK.getLabel());
        }

        dto.setAlertReasons(alertReasons);

        return dto;
    }
}
