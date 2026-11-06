-- ============================================================
-- SCRIPT DDL: MODELAGEM DO BANCO DE DADOS DA APLICAÇÃO KEEPIT
-- Suporta PostgreSQL, MySQL e H2 Database
-- ============================================================

-- 1. Tabela de Ativos de TI (Hardwares)
CREATE TABLE IF NOT EXISTS assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL, -- Ex: IMPRESSORA, ROTEADOR, COMPUTADOR, SERVIDOR
    acquisition_date DATE NOT NULL,
    usage_limit BIGINT NOT NULL, -- Ex: 10000 paginas, 5000 horas de uso
    maintenance_interval_days INT NOT NULL, -- Ex: 90, 180 dias
    last_maintenance_date DATE NOT NULL,
    current_usage BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabela de Histórico de Registro de Monitoramento / Manutenções
CREATE TABLE IF NOT EXISTS monitoring_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    current_usage BIGINT NOT NULL,
    reading_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255),
    CONSTRAINT fk_monitoring_asset FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE
);

-- 3. Cargas Iniciais para Demonstração (Seed Data)
INSERT INTO assets (name, type, acquisition_date, usage_limit, maintenance_interval_days, last_maintenance_date, current_usage)
VALUES 
('Impressora HP LaserJet Pro M404', 'IMPRESSORA', '2025-01-10', 10000, 180, '2025-03-01', 8700), -- > 85% de uso (ALERTA)
('Roteador Cisco Catalyst 9200', 'ROTEADOR', '2024-06-15', 50000, 120, '2026-03-01', 12000),    -- OK
('Computador Dell OptiPlex 7090 - Lab 01', 'COMPUTADOR', '2024-02-01', 8000, 180, '2025-09-25', 4200), -- Proximo dos 15 dias (ALERTA)
('Servidor RACK PowerEdge R750', 'SERVIDOR', '2024-11-20', 20000, 90, '2026-08-01', 5000);     -- OK
