/**
 * KeepIT - Frontend Application Script
 * Gerenciamento de Ativos de TI & Motor de Alertas Preditivos
 */

document.addEventListener('DOMContentLoaded', () => {
    // Configurações e Estado da Aplicação
    const API_BASE_URL = '/api/assets';
    let currentAssets = [];
    let activeFilter = 'ALL';
    let searchQuery = '';
    let isMockMode = false;

    // Elementos do DOM
    const gridContainer = document.getElementById('assets-grid-container');
    const totalAssetsEl = document.getElementById('total-assets-count');
    const okAssetsEl = document.getElementById('ok-assets-count');
    const alertAssetsEl = document.getElementById('alert-assets-count');
    
    const countAllTab = document.getElementById('count-all');
    const countOkTab = document.getElementById('count-ok');
    const countAlertTab = document.getElementById('count-alert');

    const searchInput = document.getElementById('search-input');
    const tabButtons = document.querySelectorAll('.tab-btn');
    const lastUpdatedEl = document.getElementById('last-updated-time');
    const apiStatusText = document.getElementById('api-status-text');

    // Modais
    const addAssetModal = document.getElementById('add-asset-modal');
    const updateUsageModal = document.getElementById('update-usage-modal');
    
    // Formulários
    const formAddAsset = document.getElementById('form-add-asset');
    const formUpdateUsage = document.getElementById('form-update-usage');

    // Botões de Abertura/Fechamento dos Modais
    document.getElementById('btn-open-add-modal').addEventListener('click', openAddModal);
    document.getElementById('btn-close-add-modal').addEventListener('click', closeAddModal);
    document.getElementById('btn-cancel-add-modal').addEventListener('click', closeAddModal);

    document.getElementById('btn-close-usage-modal').addEventListener('click', closeUsageModal);
    document.getElementById('btn-cancel-usage-modal').addEventListener('click', closeUsageModal);

    // Mock Data Inicial (Simulador Fallback caso backend Spring Boot não esteja rodando)
    const initialMockAssets = [
        {
            id: 1,
            name: "Impressora HP LaserJet Pro M404",
            type: "IMPRESSORA",
            acquisitionDate: "2025-01-10",
            usageLimit: 10000,
            maintenanceIntervalDays: 180,
            lastMaintenanceDate: "2025-03-01",
            currentUsage: 8700 // > 85% -> ALERTA
        },
        {
            id: 2,
            name: "Roteador Cisco Catalyst 9200",
            type: "ROTEADOR",
            acquisitionDate: "2024-06-15",
            usageLimit: 50000,
            maintenanceIntervalDays: 120,
            lastMaintenanceDate: getFormattedDateOffset(-30),
            currentUsage: 12000 // OK
        },
        {
            id: 3,
            name: "Computador Dell OptiPlex 7090 - Lab 01",
            type: "COMPUTADOR",
            acquisitionDate: "2024-02-01",
            usageLimit: 8000,
            maintenanceIntervalDays: 180,
            lastMaintenanceDate: getFormattedDateOffset(-170), // Faltam 10 dias (<= 15) -> ALERTA
            currentUsage: 4200
        },
        {
            id: 4,
            name: "Servidor RACK PowerEdge R750",
            type: "SERVIDOR",
            acquisitionDate: "2024-11-20",
            usageLimit: 20000,
            maintenanceIntervalDays: 90,
            lastMaintenanceDate: getFormattedDateOffset(-20),
            currentUsage: 5000 // OK
        }
    ];

    // Helper para gerar datas relativas de teste
    function getFormattedDateOffset(daysOffset) {
        const d = new Date();
        d.setDate(d.getDate() + daysOffset);
        return d.toISOString().split('T')[0];
    }

    /**
     * MOTOR DE REGRAS PREDITIVAS (Espelho do Backend Java)
     * Executado dinamicamente para garantir diagnósticos preditivos precisos.
     */
    function computePredictiveStatus(asset) {
        const usageLimit = Number(asset.usageLimit) || 1;
        const currentUsage = Number(asset.currentUsage) || 0;
        const maintenanceIntervalDays = Number(asset.maintenanceIntervalDays) || 30;

        // 1. Limite de Uso (85%)
        const usageRatio = currentUsage / usageLimit;
        const usagePercentage = Math.round(usageRatio * 1000) / 10;
        const isUsageAlert = usageRatio >= 0.85;

        // 2. Dias para Próxima Manutenção (<= 15 dias)
        const lastMaintDateStr = asset.lastMaintenanceDate || asset.acquisitionDate || new Date().toISOString().split('T')[0];
        const lastMaintDate = new Date(lastMaintDateStr);
        
        const nextMaintDate = new Date(lastMaintDate);
        nextMaintDate.setDate(nextMaintDate.getDate() + maintenanceIntervalDays);
        
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        nextMaintDate.setHours(0, 0, 0, 0);

        const diffTime = nextMaintDate.getTime() - today.getTime();
        const daysUntilMaintenance = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        const isDateAlert = daysUntilMaintenance <= 15;

        const alertReasons = [];
        if (isUsageAlert) {
            alertReasons.push(`Volume de uso atingiu ${usagePercentage}% do limite (${currentUsage.toLocaleString()} de ${usageLimit.toLocaleString()})`);
        }
        if (isDateAlert) {
            if (daysUntilMaintenance < 0) {
                alertReasons.push(`Manutenção VENCIDA há ${Math.abs(daysUntilMaintenance)} dia(s)`);
            } else {
                alertReasons.push(`Faltam apenas ${daysUntilMaintenance} dia(s) para a manutenção preventiva`);
            }
        }

        const isAlert = isUsageAlert || isDateAlert;

        return {
            ...asset,
            usagePercentage,
            daysUntilMaintenance,
            nextMaintenanceDate: nextMaintDate.toISOString().split('T')[0],
            status: isAlert ? 'ALERTA_MANUTENCAO' : 'OK',
            statusLabel: isAlert ? 'Manutenção Necessária' : 'Status OK',
            alertReasons
        };
    }

    /**
     * Carrega os dados da API REST ou faz fallback para Modo Simulação
     */
    async function loadData() {
        try {
            const response = await fetch(`${API_BASE_URL}/dashboard`);
            if (!response.ok) throw new Error('API Indisponível');
            const data = await response.json();
            
            isMockMode = false;
            apiStatusText.textContent = "Conectado ao Servidor Spring Boot (Java API)";
            apiStatusText.parentElement.querySelector('.pulse-dot').style.backgroundColor = 'var(--color-ok)';

            // Mapear ativos da resposta da API Spring Boot
            const allAssets = [...(data.okAssets || []), ...(data.alertAssets || [])];
            currentAssets = allAssets;
            renderApp();
        } catch (error) {
            // Em caso de API indisponível, usa dados em memória locais
            isMockMode = true;
            apiStatusText.textContent = "Modo de Simulação Preditiva Local (Stand-alone)";
            apiStatusText.parentElement.querySelector('.pulse-dot').style.backgroundColor = 'var(--color-warning)';

            if (!localStorage.getItem('keepit_mock_assets')) {
                localStorage.setItem('keepit_mock_assets', JSON.stringify(initialMockAssets));
            }
            const rawMock = JSON.parse(localStorage.getItem('keepit_mock_assets'));
            currentAssets = rawMock.map(computePredictiveStatus);
            renderApp();
        }
    }

    /**
     * Renderiza o Dashboard (Cards de Métricas e Grid de Equipamentos)
     */
    function renderApp() {
        // Recalcular status preditivo para todos os ativos
        const processedAssets = currentAssets.map(computePredictiveStatus);
        
        const okList = processedAssets.filter(a => a.status === 'OK');
        const alertList = processedAssets.filter(a => a.status === 'ALERTA_MANUTENCAO');

        // Atualizar Métricas nos Cards do Topo
        totalAssetsEl.textContent = processedAssets.length;
        okAssetsEl.textContent = okList.length;
        alertAssetsEl.textContent = alertList.length;

        countAllTab.textContent = processedAssets.length;
        countOkTab.textContent = okList.length;
        countAlertTab.textContent = alertList.length;

        // Atualizar horário de atualização
        lastUpdatedEl.textContent = `Atualizado às ${new Date().toLocaleTimeString('pt-BR')}`;

        // Filtrar Ativos baseado no Tab selecionado e no Campo de Busca
        let filtered = processedAssets;
        if (activeFilter === 'OK') {
            filtered = okList;
        } else if (activeFilter === 'ALERTA') {
            filtered = alertList;
        }

        if (searchQuery.trim() !== '') {
            const query = searchQuery.toLowerCase();
            filtered = filtered.filter(a => 
                a.name.toLowerCase().includes(query) || 
                a.type.toLowerCase().includes(query) || 
                String(a.id).includes(query)
            );
        }

        // Renderizar Grid de Cards
        renderAssetsGrid(filtered);
    }

    /**
     * Gera o HTML dos Cards de Equipamentos
     */
    function renderAssetsGrid(assets) {
        if (assets.length === 0) {
            gridContainer.innerHTML = `
                <div class="empty-state">
                    <ion-icon name="search-discontent-outline" style="font-size: 3rem; color: var(--text-muted); margin-bottom: 0.5rem;"></ion-icon>
                    <h4>Nenhum equipamento encontrado</h4>
                    <p>Não há ativos que correspondam aos filtros selecionados.</p>
                </div>
            `;
            return;
        }

        gridContainer.innerHTML = assets.map(asset => {
            const isAlert = asset.status === 'ALERTA_MANUTENCAO';
            
            // Cor da Barra de Progresso
            let fillClass = 'fill-ok';
            if (asset.usagePercentage >= 85) fillClass = 'fill-alert';
            else if (asset.usagePercentage >= 70) fillClass = 'fill-warning';

            return `
                <div class="asset-card ${isAlert ? 'status-alert-border' : ''}">
                    <div class="asset-card-header">
                        <div class="asset-title-group">
                            <h4>${escapeHtml(asset.name)}</h4>
                            <div class="asset-meta">
                                <span class="type-badge">${escapeHtml(asset.type)}</span>
                                <span>ID: #${asset.id}</span>
                            </div>
                        </div>
                        <span class="status-badge ${isAlert ? 'badge-alert' : 'badge-ok'}">
                            <ion-icon name="${isAlert ? 'warning-outline' : 'checkmark-circle-outline'}"></ion-icon>
                            ${isAlert ? 'Manutenção Necessária' : 'OK'}
                        </span>
                    </div>

                    <!-- Seção de Progresso do Ciclo de Uso -->
                    <div class="usage-section">
                        <div class="usage-labels">
                            <span class="usage-title">Volume de Uso</span>
                            <span class="usage-percent ${isAlert && asset.usagePercentage >= 85 ? 'text-danger' : ''}">${asset.usagePercentage}%</span>
                        </div>
                        <div class="usage-bar-track">
                            <div class="usage-bar-fill ${fillClass}" style="width: ${Math.min(asset.usagePercentage, 100)}%;"></div>
                        </div>
                        <div style="font-size: 0.76rem; color: var(--text-muted); margin-top: 0.35rem; display: flex; justify-content: space-between;">
                            <span>Atual: <strong>${asset.currentUsage.toLocaleString()}</strong></span>
                            <span>Limite: <strong>${asset.usageLimit.toLocaleString()}</strong></span>
                        </div>

                        <div class="days-info">
                            <span>Manutenção Preventiva:</span>
                            <span class="days-count ${asset.daysUntilMaintenance <= 15 ? 'text-danger' : ''}">
                                ${asset.daysUntilMaintenance < 0 ? `Vencida (${Math.abs(asset.daysUntilMaintenance)}d)` : `${asset.daysUntilMaintenance} dias restantes`}
                            </span>
                        </div>
                    </div>

                    <!-- Caixa de Razões do Alerta Preditivo (Exibida somente se houver alerta) -->
                    ${isAlert && asset.alertReasons && asset.alertReasons.length > 0 ? `
                        <div class="alert-reasons-box">
                            <strong><ion-icon name="alert-circle-outline"></ion-icon> Diagnóstico do Motor Preditivo:</strong>
                            ${asset.alertReasons.map(reason => `<div>&bull; ${escapeHtml(reason)}</div>`).join('')}
                        </div>
                    ` : ''}

                    <!-- Ações Rápidas do Card -->
                    <div class="asset-card-actions">
                        <button class="btn btn-action btn-update-usage" data-id="${asset.id}" data-name="${escapeHtml(asset.name)}" data-usage="${asset.currentUsage}" data-limit="${asset.usageLimit}">
                            <ion-icon name="speedometer-outline"></ion-icon>
                            Atualizar Uso
                        </button>
                        <button class="btn btn-action btn-action-success btn-record-maintenance" data-id="${asset.id}">
                            <ion-icon name="build-outline"></ion-icon>
                            Manutenção OK
                        </button>
                    </div>
                </div>
            `;
        }).join('');

        // Event Listeners nos Botões dos Cards
        document.querySelectorAll('.btn-update-usage').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const target = e.currentTarget;
                openUsageModal(
                    target.getAttribute('data-id'),
                    target.getAttribute('data-name'),
                    target.getAttribute('data-usage'),
                    target.getAttribute('data-limit')
                );
            });
        });

        document.querySelectorAll('.btn-record-maintenance').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const assetId = e.currentTarget.getAttribute('data-id');
                handleRecordMaintenance(assetId);
            });
        });
    }

    // Handlers dos Modais
    function openAddModal() {
        formAddAsset.reset();
        document.getElementById('asset-acquisition').value = new Date().toISOString().split('T')[0];
        document.getElementById('asset-last-maint').value = new Date().toISOString().split('T')[0];
        addAssetModal.classList.add('active');
    }

    function closeAddModal() {
        addAssetModal.classList.remove('active');
    }

    function openUsageModal(id, name, usage, limit) {
        document.getElementById('update-asset-id').value = id;
        document.getElementById('update-asset-title').querySelector('span').textContent = name;
        document.getElementById('update-current-usage').value = usage;
        document.getElementById('update-usage-limit-hint').textContent = `Limite cadastrado: ${Number(limit).toLocaleString()}`;
        document.getElementById('update-notes').value = '';
        updateUsageModal.classList.add('active');
    }

    function closeUsageModal() {
        updateUsageModal.classList.remove('active');
    }

    // Submissão do Formulário de Cadastro de Ativo
    formAddAsset.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const newAsset = {
            name: document.getElementById('asset-name').value.trim(),
            type: document.getElementById('asset-type').value,
            acquisitionDate: document.getElementById('asset-acquisition').value,
            usageLimit: Number(document.getElementById('asset-limit').value),
            maintenanceIntervalDays: Number(document.getElementById('asset-interval').value),
            lastMaintenanceDate: document.getElementById('asset-last-maint').value || document.getElementById('asset-acquisition').value,
            currentUsage: Number(document.getElementById('asset-initial-usage').value || 0)
        };

        if (isMockMode) {
            const rawMock = JSON.parse(localStorage.getItem('keepit_mock_assets') || '[]');
            newAsset.id = Date.now();
            rawMock.push(newAsset);
            localStorage.setItem('keepit_mock_assets', JSON.stringify(rawMock));
            closeAddModal();
            loadData();
        } else {
            try {
                const res = await fetch(API_BASE_URL, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(newAsset)
                });
                if (res.ok) {
                    closeAddModal();
                    loadData();
                } else {
                    alert('Erro ao cadastrar ativo na API Java Spring Boot.');
                }
            } catch (err) {
                alert('Falha na comunicação com o servidor.');
            }
        }
    });

    // Submissão da Atualização do Volume de Uso
    formUpdateUsage.addEventListener('submit', async (e) => {
        e.preventDefault();

        const assetId = Number(document.getElementById('update-asset-id').value);
        const newUsage = Number(document.getElementById('update-current-usage').value);
        const notes = document.getElementById('update-notes').value.trim();

        if (isMockMode) {
            const rawMock = JSON.parse(localStorage.getItem('keepit_mock_assets') || '[]');
            const index = rawMock.findIndex(a => a.id === assetId);
            if (index !== -1) {
                rawMock[index].currentUsage = newUsage;
                localStorage.setItem('keepit_mock_assets', JSON.stringify(rawMock));
            }
            closeUsageModal();
            loadData();
        } else {
            try {
                const res = await fetch(`${API_BASE_URL}/${assetId}/usage`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ currentUsage: newUsage, notes })
                });
                if (res.ok) {
                    closeUsageModal();
                    loadData();
                }
            } catch (err) {
                alert('Erro ao atualizar uso no servidor.');
            }
        }
    });

    // Registrar Realização de Manutenção Preventiva
    async function handleRecordMaintenance(assetId) {
        if (!confirm('Confirmar realização de manutenção preventiva neste equipamento? A data da última manutenção será atualizada para hoje.')) {
            return;
        }

        const idNum = Number(assetId);
        if (isMockMode) {
            const rawMock = JSON.parse(localStorage.getItem('keepit_mock_assets') || '[]');
            const index = rawMock.findIndex(a => a.id === idNum);
            if (index !== -1) {
                rawMock[index].lastMaintenanceDate = new Date().toISOString().split('T')[0];
                localStorage.setItem('keepit_mock_assets', JSON.stringify(rawMock));
            }
            loadData();
        } else {
            try {
                const res = await fetch(`${API_BASE_URL}/${idNum}/maintenance`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ notes: 'Manutenção registrada via Dashboard' })
                });
                if (res.ok) {
                    loadData();
                }
            } catch (err) {
                alert('Erro ao registrar manutenção na API.');
            }
        }
    }

    // Filtros por Tabs e Pesquisa
    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            tabButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            activeFilter = btn.getAttribute('data-filter');
            renderApp();
        });
    });

    searchInput.addEventListener('input', (e) => {
        searchQuery = e.target.value;
        renderApp();
    });

    // Função utilitária para sanitizar HTML
    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    // Inicialização
    loadData();
});
