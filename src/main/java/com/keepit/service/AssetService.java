package com.keepit.service;

import com.keepit.dto.AssetDTO;
import com.keepit.dto.DashboardSummaryDTO;
import com.keepit.model.Asset;
import com.keepit.model.AssetStatus;
import com.keepit.model.MonitoringLog;
import com.keepit.repository.AssetRepository;
import com.keepit.repository.MonitoringLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final MonitoringLogRepository monitoringLogRepository;
    private final PredictiveEngineService predictiveEngineService;

    public AssetService(AssetRepository assetRepository, 
                        MonitoringLogRepository monitoringLogRepository, 
                        PredictiveEngineService predictiveEngineService) {
        this.assetRepository = assetRepository;
        this.monitoringLogRepository = monitoringLogRepository;
        this.predictiveEngineService = predictiveEngineService;
    }

    @Transactional
    public AssetDTO createAsset(Asset asset) {
        if (asset.getLastMaintenanceDate() == null) {
            asset.setLastMaintenanceDate(asset.getAcquisitionDate() != null ? asset.getAcquisitionDate() : LocalDate.now());
        }
        if (asset.getCurrentUsage() == null) {
            asset.setCurrentUsage(0L);
        }
        Asset savedAsset = assetRepository.save(asset);
        
        // Registrar log inicial de monitoramento
        monitoringLogRepository.save(new MonitoringLog(savedAsset, savedAsset.getCurrentUsage(), "Cadastro inicial do ativo"));
        
        return predictiveEngineService.processAssetPredictiveStatus(savedAsset);
    }

    @Transactional(readOnly = true)
    public List<AssetDTO> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(predictiveEngineService::processAssetPredictiveStatus)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssetDTO getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com o ID: " + id));
        return predictiveEngineService.processAssetPredictiveStatus(asset);
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary() {
        List<AssetDTO> allDTOs = getAllAssets();

        List<AssetDTO> okAssets = allDTOs.stream()
                .filter(a -> a.getStatus() == AssetStatus.OK)
                .collect(Collectors.toList());

        List<AssetDTO> alertAssets = allDTOs.stream()
                .filter(a -> a.getStatus() == AssetStatus.ALERTA_MANUTENCAO)
                .collect(Collectors.toList());

        return new DashboardSummaryDTO(
                allDTOs.size(),
                okAssets.size(),
                alertAssets.size(),
                okAssets,
                alertAssets
        );
    }

    @Transactional
    public AssetDTO updateUsage(Long assetId, Long newUsage, String notes) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com o ID: " + assetId));

        asset.setCurrentUsage(newUsage);
        Asset updatedAsset = assetRepository.save(asset);

        // Registrar no histórico de monitoramento
        String logNote = (notes != null && !notes.isBlank()) ? notes : "Atualização de volume de uso";
        monitoringLogRepository.save(new MonitoringLog(updatedAsset, newUsage, logNote));

        return predictiveEngineService.processAssetPredictiveStatus(updatedAsset);
    }

    @Transactional
    public AssetDTO recordMaintenance(Long assetId, String notes) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com o ID: " + assetId));

        asset.setLastMaintenanceDate(LocalDate.now());
        // Ao realizar a manutenção, zeramos ou ajustamos o ciclo se aplicável (ex: impressora limpa)
        Asset updatedAsset = assetRepository.save(asset);

        String logNote = (notes != null && !notes.isBlank()) ? notes : "Manutenção preventiva/corretiva realizada";
        monitoringLogRepository.save(new MonitoringLog(updatedAsset, asset.getCurrentUsage(), logNote));

        return predictiveEngineService.processAssetPredictiveStatus(updatedAsset);
    }
}
