package com.keepit.controller;

import com.keepit.dto.AssetDTO;
import com.keepit.dto.DashboardSummaryDTO;
import com.keepit.model.Asset;
import com.keepit.service.AssetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*") // Habilita CORS para integração com o frontend
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    /**
     * Endpoint para obter o resumo do Dashboard (Total, Contagem OK, Contagem Alertas e listas).
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
        DashboardSummaryDTO summary = assetService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Endpoint para listar todos os ativos com seus diagnósticos preditivos.
     */
    @GetMapping
    public ResponseEntity<List<AssetDTO>> getAllAssets() {
        List<AssetDTO> assets = assetService.getAllAssets();
        return ResponseEntity.ok(assets);
    }

    /**
     * Endpoint para buscar um ativo específico pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssetDTO> getAssetById(@PathVariable Long id) {
        AssetDTO asset = assetService.getAssetById(id);
        return ResponseEntity.ok(asset);
    }

    /**
     * Endpoint para cadastrar um novo ativo de TI.
     */
    @PostMapping
    public ResponseEntity<AssetDTO> createAsset(@RequestBody Asset asset) {
        AssetDTO createdAsset = assetService.createAsset(asset);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAsset);
    }

    /**
     * Endpoint para registrar o monitoramento de uso (atualização de volume atual).
     */
    @PutMapping("/{id}/usage")
    public ResponseEntity<AssetDTO> updateUsage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        
        Long currentUsage = Long.parseLong(payload.get("currentUsage").toString());
        String notes = payload.containsKey("notes") ? (String) payload.get("notes") : null;

        AssetDTO updatedAsset = assetService.updateUsage(id, currentUsage, notes);
        return ResponseEntity.ok(updatedAsset);
    }

    /**
     * Endpoint para registrar a realização de uma manutenção preventiva/corretiva no equipamento.
     */
    @PostMapping("/{id}/maintenance")
    public ResponseEntity<AssetDTO> recordMaintenance(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload) {
        
        String notes = (payload != null && payload.containsKey("notes")) ? payload.get("notes") : null;
        AssetDTO updatedAsset = assetService.recordMaintenance(id, notes);
        return ResponseEntity.ok(updatedAsset);
    }
}
