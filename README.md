# KeepIT – Gestão de Ativos de TI & Manutenção Preditiva

[![GitHub Pages](https://img.shields.io/badge/Demo-Acesso_Online_Imediato-brightgreen?style=for-the-badge&logo=github)](https://michelschiavo08-beep.github.io/keepit/)
[![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.3-brightgreen?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)

O **KeepIT** é um sistema web desenvolvido como MVP para solucionar a gestão de hardwares e a prevenção de falhas em pequenas empresas e escolas (impressoras, roteadores, computadores, servidores e nobreaks). O sistema monitora o ciclo de utilização e prazos de manutenção, alertando os técnicos de forma preditiva antes que as paradas ocorram.

---

## 🌐 Links Principais da Entrega

* 🚀 **Sistema no Ar (Acesso Imediato sem Instalar Nada):** [https://michelschiavo08-beep.github.io/keepit/](https://michelschiavo08-beep.github.io/keepit/)
* 📋 **Especificação Técnica Completa (Spec Final):** [`spec.md`](spec.md)
* 📝 **Registro de Decisões e Revisão Humana:** [`CHANGELOG.md`](CHANGELOG.md)
* 🗄️ **Script do Banco de Dados Relacional:** [`database/schema.sql`](database/schema.sql)
* ⚙️ **Variáveis de Ambiente de Exemplo:** [`.env.example`](.env.example)
* 👥 **Colaborador Convidado:** `rafaelpitwak`

---

## 💡 Motor de Alertas Preditivos

O status do equipamento transiciona automaticamente para **"Manutenção Necessária"** quando:
1. **Limite de Ciclo de Uso:** Atinge ou ultrapassa **85%** do limite cadastrado (ex.: 8.700 de 10.000 páginas);
2. **Prazo de Manutenção Preventiva:** Faltam **15 dias ou menos** até a data programada para a próxima revisão (`data da última manutenção + intervalo em dias`).

---

## 🚀 Como Executar Localmente

### Opção 1: Via Maven (Spring Boot + Banco H2 Embutido)
Não é necessário instalar nenhum banco de dados prévio. O sistema sobe com H2 em memória:
```bash
git clone https://github.com/michelschiavo08-beep/keepit.git
cd keepit
mvn spring-boot:run
```
* **Dashboard:** [http://localhost:8080](http://localhost:8080)
* **Console H2:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:keepitdb`, Usuário: `sa`, Senha: em branco)

### Opção 2: Com Banco MySQL ou PostgreSQL
1. Crie a base de dados `keepit_db` e importe o script [`database/schema.sql`](database/schema.sql).
2. Copie [`.env.example`](.env.example) para `.env` e ajuste suas credenciais locais.
3. Inicie com `mvn spring-boot:run`.

---

## 📊 Endpoints REST da API

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/assets/dashboard` | Retorna métricas consolidadas e listas de ativos OK e sob Alerta |
| `GET` | `/api/assets` | Lista todos os ativos com diagnóstico preditivo calculado |
| `GET` | `/api/assets/{id}` | Consulta detalhes de um ativo específico |
| `POST` | `/api/assets` | Cadastra novo equipamento de TI |
| `PUT` | `/api/assets/{id}/usage` | Atualiza o volume de uso acumulado e registra no histórico |
| `POST` | `/api/assets/{id}/maintenance` | Registra a realização de manutenção preventiva/corretiva |

Para mais detalhes sobre requisitos, modelagem relacional completa, dicionário de dados e roteiro de testes, consulte a [**`spec.md`**](spec.md).
