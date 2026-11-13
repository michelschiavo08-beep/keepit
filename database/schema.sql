-- ============================================================
-- PROJETO: KeepIT - Gestão de Ativos de TI & Manutenção Preditiva
-- ARQUIVO: database/schema.sql
-- SGBD SUPORTADOS: MySQL 8+, PostgreSQL 14+, H2 Database
-- ============================================================

-- 1. Criação da Tabela de Ativos de TI (Hardwares)
CREATE TABLE IF NOT EXISTS assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL, -- Ex: IMPRESSORA, ROTEADOR, COMPUTADOR, SERVIDOR, NOBREAK
    acquisition_date DATE NOT NULL,
    usage_limit BIGINT NOT NULL, -- Limite de ciclo (ex: 10000 páginas, 5000 horas)
    maintenance_interval_days INT NOT NULL, -- Periodicidade da manutenção preventiva em dias
    last_maintenance_date DATE NOT NULL, -- Data base da última manutenção preventiva realizada
    current_usage BIGINT NOT NULL DEFAULT 0, -- Leitura atual do volume de uso acumulado
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Criação da Tabela de Histórico de Registro de Monitoramento / Manutenções
CREATE TABLE IF NOT EXISTS monitoring_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    current_usage BIGINT NOT NULL,
    reading_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255),
    CONSTRAINT fk_monitoring_asset FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE
);

-- 3. Carga Inicial de Dados de Demonstração e Testes (Seed Data)
INSERT INTO assets (name, type, acquisition_date, usage_limit, maintenance_interval_days, last_maintenance_date, current_usage)
VALUES 
('Impressora HP LaserJet Pro M404', 'IMPRESSORA', '2025-01-10', 10000, 180, '2025-03-01', 8700), -- Alerta Preditivo: uso em 87% (>= 85%)
('Roteador Cisco Catalyst 9200', 'ROTEADOR', '2024-06-15', 50000, 120, '2026-03-01', 12000),    -- Status OK: uso em 24%, manutenção distante
('Computador Dell OptiPlex 7090 - Lab 01', 'COMPUTADOR', '2024-02-01', 8000, 180, '2025-09-25', 4200), -- Alerta Preditivo: faltam <= 15 dias para manutenção
('Servidor RACK PowerEdge R750', 'SERVIDOR', '2024-11-20', 20000, 90, '2026-08-01', 5000);     -- Status OK
