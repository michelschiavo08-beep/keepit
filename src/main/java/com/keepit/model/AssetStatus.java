package com.keepit.model;

/**
 * Enum representando os status preditivos calculados para um ativo de TI.
 */
public enum AssetStatus {
    OK("Status OK", "Equipamento em condições normais de uso"),
    ALERTA_MANUTENCAO("Manutenção Necessária", "Alerta preditivo ativado: limite de uso ou prazo de manutenção atingido");

    private final String label;
    private final String description;

    AssetStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
