# Especificação Técnica Final (Spec Final) - KeepIT

> **Sistema de Gestão de Ativos de TI & Manutenção Preditiva**  
> **Repositório GitHub:** [https://github.com/michelschiavo08-beep/keepit](https://github.com/michelschiavo08-beep/keepit)  
> **Aplicação Publicada (Acesso Online Imediato):** [https://michelschiavo08-beep.github.io/keepit/](https://michelschiavo08-beep.github.io/keepit/)  
> **Colaborador Convidado:** `rafaelpitwak`

---

## 1. Objetivo do Sistema

O **KeepIT** é um sistema web desenvolvido para solucionar a falta de controle preventivo sobre hardwares e ativos de tecnologia em pequenas empresas, laboratórios de informática e escolas. 

Em ambientes educacionais e pequenos negócios, equipamentos como impressoras de provas, roteadores de salas de aula, computadores de laboratório e servidores sofrem paradas repentinas por falta de acompanhamento de ciclo de uso ou expiração de prazos de manutenção preventiva básica (ex: limpeza de cabeças de impressão, substituição de roletes, troca de pasta térmica).

O KeepIT resolve esse problema através de um **Motor de Alertas Preditivos** que cruza o volume de uso acumulado e a data da última manutenção com a data atual, sinalizando em tempo real quais equipamentos exigem atenção imediata antes que quebrem.

---

## 2. Usuários e Perfis

* **Técnico / Responsável de TI:** Cadastra novos hardwares, alimenta as medições de uso periódicas (ex: total de páginas impressas lidas no contador da impressora, horas de uso), visualiza diagnósticos do motor preditivo e registra a realização das manutenções.
* **Gestor / Coordenador da Escola ou Empresa:** Acompanha os indicadores visuais do dashboard executivo (total de equipamentos saudáveis vs. em estado de alerta) para planejar custos de peças e evitar interrupções operacionais.

---

## 3. Casos de Uso

* **UC01 - Cadastrar Ativo de TI:** Informar nome do equipamento, tipo (Impressora, Roteador, Computador, Servidor, Nobreak), data de aquisição, limite de ciclo de uso (ex.: 10.000 páginas) e intervalo de manutenção em dias (ex.: 180 dias).
* **UC02 - Registrar Volume de Uso Atual (Monitoramento):** Lançar a nova contagem acumulada de utilização e observações da medição.
* **UC03 - Processamento Preditivo e Disparo de Alertas:** O motor avalia as métricas do ativo e gera o status `OK` (verde) ou `ALERTA_MANUTENCAO` (vermelho), explicitando o motivo do alerta.
* **UC04 - Registrar Realização de Manutenção:** Ao efetuar o serviço preventivo, o sistema atualiza a data da última manutenção para a data corrente e reinicia a contagem temporal do ciclo.
* **UC05 - Visualizar Dashboard e Filtrar Ativos:** Exibir cartões de métricas consolidadas (Total, OK, Alertas) e listagem interativa com barra de busca e filtros por status.

---

## 4. Requisitos do Sistema

### 4.1 Requisitos Funcionais (RF)
* **RF01:** O sistema deve permitir o cadastro de ativos de TI com validação de campos obrigatórios.
* **RF02:** O sistema deve permitir atualizar o volume de uso acumulado de qualquer hardware cadastrado.
* **RF03:** O sistema deve manter histórico das leituras de uso registradas na tabela `monitoring_logs`.
* **RF04:** O sistema deve calcular automaticamente o percentual de ciclo de uso atingido em relação ao limite estipulado.
* **RF05:** O sistema deve calcular os dias restantes até a próxima manutenção preventiva baseando-se na data da última revisão somada ao intervalo de manutenção em dias.
* **RF06:** O sistema deve disparar alerta de **"Manutenção Necessária"** caso o equipamento atinja **85% ou mais do limite de uso** OU faltem **15 dias ou menos para a manutenção programada** (incluindo manutenções vencidas).
* **RF07:** O sistema deve permitir registrar a conclusão de manutenção, atualizando o ciclo preventivo.
* **RF08:** O sistema deve disponibilizar Dashboard responsivo com divisão visual clara entre equipamentos saudáveis (OK) e sob alerta de manutenção.

### 4.2 Requisitos Não-Funcionais (RNF)
* **RNF01 - Portabilidade e Execução Sem Instalação:** A interface deve estar hospedada publicamente via GitHub Pages, permitindo uso imediato em qualquer navegador moderno sem necessidade de instalação local prévia pelo avaliador.
* **RNF02 - Execução Local com Configuração Zero:** O backend Java Spring Boot deve vir configurado por padrão com banco de dados relacional em memória H2, garantindo que o avaliador execute a aplicação local com comando único Maven (`mvn spring-boot:run`), sem obrigatoriedade de instalar previamente instâncias de MySQL ou PostgreSQL.
* **RNF03 - Segurança:** Nenhuma senha, token ou credencial sensível deve constar versionada no repositório. O suporte a banco de dados externo deve ser orientado via variáveis de ambiente com template `.env.example`.
* **RNF04 - Consultas Seguras:** Todas as operações com banco devem utilizar mapeamento ORM / Spring Data JPA com consultas parametrizadas, prevenindo ataques de SQL Injection.

---

## 5. Regras de Negócio e Lógica do Motor de Alertas Preditivos

A lógica reside no serviço [`PredictiveEngineService.java`](file:///c:/Users/Aluno/Documents/desenvolvimentomichel/src/main/java/com/keepit/service/PredictiveEngineService.java) e é espelhada no cliente para o modo online.

### 5.1 Regra de Limite de Ciclo de Uso
$$\text{Razão de Uso} = \frac{\text{currentUsage}}{\text{usageLimit}}$$
$$\text{Alerta por Uso} \iff \text{Razão de Uso} \ge 0.85 \quad (85\%)$$

*Exemplo:* Impressora com limite de 10.000 páginas. Ao atingir 8.500 páginas (85%), o status muda imediatamente de `OK` para `Manutenção Necessária`.

### 5.2 Regra de Prazo de Manutenção Preventiva
$$\text{Data da Próxima Manutenção} = \text{lastMaintenanceDate} + \text{maintenanceIntervalDays}$$
$$\text{Dias Restantes} = \text{Data da Próxima Manutenção} - \text{Data Atual}$$
$$\text{Alerta por Prazo} \iff \text{Dias Restantes} \le 15 \text{ dias}$$

*Exemplo:* Computador revisado há 166 dias com intervalo estipulado de 180 dias. Faltam 14 dias para a revisão recomendada; o sistema dispara alerta preditivo preventivo.

### 5.3 Diagnóstico Integrado
Se qualquer uma das condições for satisfeita, o ativo recebe o status `ALERTA_MANUTENCAO`, com a exibição do diagnóstico detalhado no cartão do equipamento.

---

## 6. Modelo de Dados (Banco de Dados Relacional)

O banco de dados relacional possui duas entidades conectadas por chave estrangeira, com integridade referencial e deleção em cascata.

### 6.1 Tabela de Entidades e Relacionamentos

| Tabela | Campos Principais | Chave Primária | Relacionamentos |
|---|---|---|---|
| `assets` | `id`, `name`, `type`, `acquisition_date`, `usage_limit`, `maintenance_interval_days`, `last_maintenance_date`, `current_usage`, `created_at` | `id` | — (Tabela principal de hardwares) |
| `monitoring_logs` | `id`, `asset_id`, `current_usage`, `reading_date`, `notes` | `id` | `asset_id` $\rightarrow$ `assets.id` (Chave Estrangeira, `ON DELETE CASCADE`) |

### 6.2 Dicionário de Dados

#### Tabela `assets`
* `id` (BIGINT, Auto Incremento, PK): Identificador único do hardware.
* `name` (VARCHAR(100), NOT NULL): Nome amigável do equipamento.
* `type` (VARCHAR(50), NOT NULL): Categoria (`IMPRESSORA`, `ROTEADOR`, `COMPUTADOR`, `SERVIDOR`, `NOBREAK`, `OUTRO`).
* `acquisition_date` (DATE, NOT NULL): Data da compra ou tombamento do patrimônio.
* `usage_limit` (BIGINT, NOT NULL): Limite do ciclo de uso do fabricante ou padrão interno.
* `maintenance_interval_days` (INT, NOT NULL): Frequência recomendada de manutenções em dias corridos.
* `last_maintenance_date` (DATE, NOT NULL): Data em que ocorreu a última manutenção preventiva/corretiva.
* `current_usage` (BIGINT, DEFAULT 0): Leitura mais recente do uso acumulado.
* `created_at` (TIMESTAMP): Data de inclusão do registro no sistema.

#### Tabela `monitoring_logs`
* `id` (BIGINT, Auto Incremento, PK): Identificador da medição.
* `asset_id` (BIGINT, NOT NULL, FK): Referência ao ativo monitorado.
* `current_usage` (BIGINT, NOT NULL): Volume registrado na data da medição.
* `reading_date` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP): Data/hora da leitura.
* `notes` (VARCHAR(255)): Observações e apontamentos do operador.

---

## 7. Decisão de Stack Tecnológica

| Camada | Tecnologia Escolhida | Justificativa |
|---|---|---|
| **Linguagem Backend** | Java 17+ (LTS) | Robustez, tipagem estática e forte aderência a projetos corporativos de TI. |
| **Framework Backend** | Spring Boot 3 (Spring Web, Spring Data JPA) | Produtividade, inversão de controle, servidor Tomcat embutido e ORM Hibernate integrado. |
| **Banco de Dados** | H2 Database (Padrão) / MySQL 8+ / PostgreSQL 14+ | H2 possibilita execução imediata pelo avaliador sem dependências externas; suporte a MySQL/PostgreSQL via variáveis de ambiente para ambientes produtivos. |
| **Frontend** | HTML5, CSS3 Moderno, JavaScript Vanilla | Sem dependências pesadas de build (npm/webpack), permitindo funcionamento duplo: servido via Spring Boot ou publicado como SPA leve no GitHub Pages. |
| **Controle de Versão** | Git & GitHub | Rastreabilidade de commits, histórico real e colaboração com o professor (`rafaelpitwak`). |

---

## 8. Instalação e Execução

### 8.1 Ambiente
* **Linguagem e Versão:** Java 17 ou superior (testado e homologado em **Java 21 LTS**).
* **Gerenciador de Dependências:** Apache Maven 3.8+.
* **Servidor Local Embutido:** Spring Boot Tomcat na porta `8080`.
* **Sistema Operacional Homologado:** Windows 11 (totalmente compatível com Ubuntu/Debian e macOS).

### 8.2 Banco de Dados
* **SGBD Padrão de Execução Local:** H2 Database em memória (não requer instalação prévia, cria as tabelas automaticamente via `database/schema.sql` ao subir o backend).
* **SGBD Homologado para Produção:** MySQL 8+ ou PostgreSQL 14+.
* **Nome do Banco:** `keepit_db` (ao utilizar MySQL ou PostgreSQL).
* **Caminho do Script SQL no Repositório:** `database/schema.sql`.
* **Como Executar o Script Manualmente (Caso use MySQL local):**
  ```bash
  mysql -u root -p -e "CREATE DATABASE keepit_db;"
  mysql -u root -p keepit_db < database/schema.sql
  ```
  *(Ou importar o arquivo `database/schema.sql` via phpMyAdmin / MySQL Workbench).*
* **Dados Iniciais (Seed):** O arquivo `database/schema.sql` já contém a inserção automática de 4 equipamentos com diferentes cenários (impressora em alerta por uso > 85%, computador em alerta por prazo < 15 dias, servidor e roteador com status OK).

### 8.3 Variáveis de Ambiente
O repositório possui o arquivo [`.env.example`](file:///c:/Users/Aluno/Documents/desenvolvimentomichel/.env.example). Para configurar um banco externo, copie-o para `.env`:

```bash
# .env.example
PORT=8080
DB_HOST=localhost
DB_PORT=3306
DB_NAME=keepit_db
DB_USER=root
DB_PASS=sua_senha_aqui
DB_URL=jdbc:mysql://localhost:3306/keepit_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_DRIVER=com.mysql.cj.jdbc.Driver
```

#### Descrição das Variáveis
* `PORT`: Porta TCP do servidor web (Padrão: `8080`).
* `DB_HOST`: Host do servidor de banco de dados (ex: `localhost`).
* `DB_PORT`: Porta do SGBD (ex: `3306` para MySQL, `5432` para PostgreSQL).
* `DB_NAME`: Nome do banco relacional criado (ex: `keepit_db`).
* `DB_USER`: Usuário do banco de dados (ex: `root`).
* `DB_PASS`: Senha do banco de dados (deve permanecer fora do Git, configurada apenas localmente).
* `DB_URL`: String JDBC completa montada.
* `DB_DRIVER`: Driver JDBC (`com.mysql.cj.jdbc.Driver` para MySQL ou `org.postgresql.Driver` para PostgreSQL).

---

### 8.4 Passo a Passo de Instalação e Execução

#### Opção A: Execução Local Completa com Backend Java Spring Boot
Siga os 5 passos abaixo em um computador com Java 17+ e Maven instalados:

1. **Clonar o Repositório:**
   ```bash
   git clone https://github.com/michelschiavo08-beep/keepit.git
   cd keepit
   ```

2. **Verificar Dependências do Maven:**
   As dependências são baixadas automaticamente pelo Maven a partir do `pom.xml`. Não há dependências manuais adicionais.

3. **(Opcional) Configurar Banco MySQL:**
   *Se quiser rodar direto com o banco H2 embutido de teste, pule este passo.*  
   Para usar MySQL, crie a base `keepit_db`, importe `database/schema.sql` e crie o arquivo `.env` com base no `.env.example`.

4. **Iniciar a Aplicação:**
   Execute no terminal da pasta do projeto:
   ```bash
   mvn spring-boot:run
   ```

5. **Acessar o Sistema no Navegador:**
   * **Dashboard do Sistema:** [http://localhost:8080](http://localhost:8080)
   * **Console Web do Banco H2:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
     *(Dados de login H2: Driver: `org.h2.Driver` | JDBC URL: `jdbc:h2:mem:keepitdb` | Usuário: `sa` | Senha: em branco)*

---

#### Opção B: Uso Imediato Online (Sem Instalar Nada)
Para atender ao requisito de avaliação sem instalação prévia:

1. Abra qualquer navegador (incluindo em aba anônima).
2. Acesse a URL pública: **[https://michelschiavo08-beep.github.io/keepit/](https://michelschiavo08-beep.github.io/keepit/)**
3. A aplicação carregará a interface com o motor preditivo e persistência local, permitindo cadastrar, atualizar leituras e testar todas as regras de negócio em tempo real.

---

### 8.5 Como Testar

#### Roteiro do Fluxo Principal (Validação do Motor Preditivo)

1. **Visualizar o Dashboard Inicial:**
   * Observe os cards do topo: **Total de Ativos (4)**, **Status OK (2)**, **Manutenção Necessária (2)**.
   * Observe que a *Impressora HP LaserJet* exibe a badge vermelha indicando alerta preditivo: *"Volume de uso atingiu 87.0% do limite (8.700 de 10.000)"*.
   * Observe que o *Computador Dell OptiPlex* exibe a badge vermelha indicando alerta por prazo: *"Faltam apenas 10 dia(s) para a manutenção preventiva"*.

2. **Testar Cadastro de Novo Equipamento:**
   * Clique no botão **"+ Novo Ativo"** no canto superior direito.
   * Preencha:
     * **Nome:** *Nobreak APC Smart-UPS 1500VA*
     * **Tipo:** *Nobreak / UPS*
     * **Data de Aquisição:** *Data de hoje*
     * **Limite de Ciclo de Uso:** `5000`
     * **Intervalo de Manutenção (Dias):** `90`
     * **Data da Última Manutenção:** *Data de hoje*
     * **Volume de Uso Inicial:** `500`
   * Clique em **Salvar Equipamento**.
   * O ativo aparecerá na lista com status **OK (Verde)** e uso em 10%.

3. **Testar Disparo do Alerta Preditivo (Regra de 85% de Uso):**
   * No card do Nobreak recém-criado, clique em **"Atualizar Uso"**.
   * Altere a medição de `500` para `4300` (86% de 5000) e confirme.
   * **Resultado Esperado:** O cartão atualiza imediatamente para a cor vermelha com a badge **"Manutenção Necessária"** e detalha o motivo: *"Volume de uso atingiu 86.0% do limite (4.300 de 5.000)"*.

4. **Testar Normalização Pós-Manutenção:**
   * No equipamento que está sob alerta, clique no botão verde **"Manutenção OK"**.
   * Confirme a realização do serviço.
   * **Resultado Esperado:** A data da última manutenção é renovada para o dia atual, recalculando os prazos preventivos.

---

### 8.6 Problemas Conhecidos e Limitações do MVP
* O sistema opera com sessão única (sem controle de login/autenticação por múltiplos perfis de usuários na versão MVP).
* Os alertas são exibidos em tela no dashboard e não realizam envio externo de e-mails ou mensagens SMS/WhatsApp nesta versão inicial.

---

## 9. Checklist de Segurança e Boas Práticas

| Item de Segurança | Situação | Observações |
|---|---|---|
| **Credenciais no Código** | Conforme | Nenhuma credencial ou senha exposta. Arquivo `.env` inserido no `.gitignore`. Template `.env.example` sem dados reais. |
| **SQL Injection** | Conforme | Utilização estrita do Spring Data JPA / Hibernate com Named Parameters e Prepared Statements em todas as operações. |
| **Cross-Site Scripting (XSS)** | Conforme | O frontend sanitiza inputs dinâmicos através da função `escapeHtml()` antes de injetar conteúdo no DOM. |
| **Integridade de Dados** | Conforme | Chave estrangeira `fk_monitoring_asset` com deleção em cascata garantindo coerência relacional. |
