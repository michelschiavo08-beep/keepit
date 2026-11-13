# Registro de Decisões e Changelog do Projeto KeepIT

Este documento registra a evolução do projeto, as propostas geradas pela Inteligência Artificial (Google Antigravity), a **revisão crítica e decisões tomadas pelos desenvolvedores**, bem como as correções realizadas ao longo do ciclo de vida da aplicação.

---

## [1.2.0] - 2026-11-13: Documentação Final, Segurança e Deploy Público

### 1. Documentação de Instalação e Execução (Spec Final)
* **Proposta do Agente IA:** Manter apenas um README genérico com instruções superficiais.
* **Revisão Humana / Decisão:** Criação do documento formal [`spec.md`](spec.md) com todas as seções exigidas na atividade: requisitos funcionais/não-funcionais, modelagem com tabela de relacionamentos, variáveis de ambiente, roteiro de testes guiado e limitações conhecidas.

### 2. Gestão de Credenciais e Segurança
* **Proposta do Agente IA:** Configurar senhas e credenciais diretamente no `application.properties`.
* **Revisão Humana / Decisão:** Identificado risco de segurança conforme o Anexo B da apostila. Foram introduzidas variáveis de ambiente com defaults seguros e criado o arquivo [`.env.example`](.env.example). Adicionado `.env` ao [`.gitignore`](.gitignore) para prevenir vazamento acidental de credenciais.

### 3. Publicação Online (Acesso Sem Instalação)
* **Proposta do Agente IA:** Depender exclusivamente de um servidor backend com Tomcat rodando na máquina local.
* **Revisão Humana / Decisão:** O requisito da atividade exige que *"qualquer pessoa, a partir de um link, consiga usar o sistema no ar, sem instalar nada"*. Foi estruturado o suporte para deploy do frontend via **GitHub Pages**, contendo um motor de simulação e persistência local client-side que replica integralmente o motor do Spring Boot quando executado de forma independente.

---

## [1.1.0] - 2026-11-06: Versionamento Git e Integração Contínua

### 1. Inicialização do Repositório
* **Decisão:** Configuração do repositório Git local na branch `main` e conexão com o repositório remoto [https://github.com/michelschiavo08-beep/keepit.git](https://github.com/michelschiavo08-beep/keepit.git).
* **Correção de Ambiente:** Ao tentar realizar o `git push` via sandbox sem privilégio de rede, o processo congelou à espera de credenciais. Foi realizada a autenticação interativa direta com a conta do GitHub `michelschiavo08-beep`.

---

## [1.0.0] - 2026-09-04: Implementação do MVP (Backend, Motor Preditivo e Frontend)

### 1. Modelagem do Banco de Dados Relacional
* **Proposta do Agente IA:** Criar apenas a tabela `assets` com colunas de contagem simples.
* **Revisão Humana / Decisão:** Solicitada a inclusão da tabela `monitoring_logs` conectada por chave estrangeira (`fk_monitoring_asset`). Isso é fundamental para manter rastreabilidade histórica das leituras de uso e registros das manutenções passadas.
* **Arquivo Resultante:** [`database/schema.sql`](database/schema.sql).

### 2. Lógica do Motor de Alertas Preditivos (`PredictiveEngineService`)
* **Proposta do Agente IA:** Disparar alerta apenas por limite fixo de dias corridos desde a aquisição.
* **Revisão Humana / Decisão:** Refinamento da regra de negócio para atender fielmente aos dois gatilhos do MVP:
  1. **Volume de Uso:** Alerta imediato se o uso atingir ou ultrapassar 85% do limite do ciclo ($\text{currentUsage} / \text{usageLimit} \ge 0.85$).
  2. **Prazo de Manutenção Preventiva:** Alerta se faltarem 15 dias ou menos até a próxima revisão programada ($\text{lastMaintenanceDate} + \text{maintenanceIntervalDays} - \text{hoje} \le 15$).
* **Tratamento de Casos de Borda (Edge Cases):**
  - Tratamento para evitar divisão por zero se `usageLimit` for zero ou nulo.
  - Tratamento para manutenções vencidas (dias negativos), exibindo mensagem em destaque: *"Manutenção VENCIDA há X dia(s)"*.

### 3. Escolha e Configuração do SGBD (H2 vs. MySQL)
* **Proposta do Agente IA:** Exigir banco MySQL pré-instalado com credenciais fixas de usuário.
* **Revisão Humana / Decisão:** Adotar uma abordagem híbrida inteligente. O sistema utiliza **H2 Database em memória** por padrão para permitir que qualquer pessoa ou professor consiga rodar a aplicação imediatamente com `mvn spring-boot:run` sem precisar instalar e configurar um servidor MySQL local. Caso deseje usar MySQL ou PostgreSQL em ambiente corporativo, basta configurar as variáveis no `.env`.

### 4. Interface e Dashboard Responsivo
* **Proposta do Agente IA:** Interface simples baseada em tabelas HTML padrão.
* **Revisão Humana / Decisão:** Desenvolvimento de uma interface moderna em Dark Mode com Design System próprio, cards de resumo de métricas (Total, OK, Alerta), badges com animações pulsantes nos itens críticos, barras de progresso dinâmicas coloridas (verde < 70%, laranja 70-84%, vermelho >= 85%) e modais intuitivos para cadastro e registro de medições.
* **Prevenção de Vulnerabilidades:** Adicionada sanitização explícita de caracteres HTML (`escapeHtml()`) na renderização do DOM para mitigar ataques de Cross-Site Scripting (XSS).
