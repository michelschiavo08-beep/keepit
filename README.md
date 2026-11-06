# KeepIT – Gestão de Ativos de TI & Manutenção Preditiva

Um MVP para pequenas empresas e escolas: cadastro de hardwares, registro do volume de uso e alertas automáticos de manutenção.

## Stack
- **Backend:** Java 17+ / Spring Boot 3 (Web + Data JPA)
- **Banco:** H2 (padrão), PostgreSQL ou MySQL (veja `application.properties`)
- **Frontend:** HTML/CSS/JS vanilla (`src/main/resources/static`)

## Motor de Alertas Preditivos
O status vira **"Manutenção Necessária"** quando:
1. o uso atual for **≥ 85%** do limite de ciclo, **ou**
2. faltarem **≤ 15 dias** para a próxima manutenção (`última manutenção + intervalo`).

A lógica fica em `PredictiveEngineService.java`.

## Como executar
```bash
mvn spring-boot:run
```
- App: http://localhost:8080
- Console H2: http://localhost:8080/h2-console (JDBC `jdbc:h2:mem:keepitdb`, usuário `sa`)

Para uma prévia sem o backend, abra o `index.html` da raiz no navegador (modo de simulação local).

## API
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/assets/dashboard` | Resumo + listas OK/Alerta |
| GET | `/api/assets` | Lista os ativos |
| POST | `/api/assets` | Cadastra um ativo |
| PUT | `/api/assets/{id}/usage` | Atualiza o volume de uso |
| POST | `/api/assets/{id}/maintenance` | Registra uma manutenção |
