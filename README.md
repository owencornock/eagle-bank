# 🦅 Eagle Bank

A multi-module Spring Boot + React application providing a RESTful API for user, account, and transaction management at
Eagle Bank.

---

## 🚀 Features

- **Users**
    - Sign up, fetch, update, delete
    - JWT-based authentication
- **Accounts**
    - Create, list, fetch, update, delete
- **Transactions**
    - Deposit & withdrawal
    - List & fetch history
- **Docs**
    - OpenAPI (Swagger UI)
    - Postman collection

---

## 📦 Modules

eagle-bank/ ← root
├── eagle-bank-domain ← domain model & value types
├── eagle-bank-repository ← Spring Data adapters
├── eagle-bank-logic ← services & business logic
├── eagle-bank-app ← Spring Boot application And API

---

## 🛠 Prerequisites

- Java 21 (via SDKMAN / homebrew / your JDK of choice)
- Gradle (wrapper included)
- (Optional) Docker & Docker Compose if you containerize

---

## ⚙️ Backend Setup

1. **Configure**  
   Copy and tweak `eagle-bank-app/src/main/resources/application.yml` as needed (e.g. JWT secret, H2 vs Postgres).

2. **Build & run**
   ```bash
   # from project root
   ./gradlew clean build
   ./gradlew :eagle-bank-app:bootRun

3. **Smoke Test**
    ```bash
   curl -X GET http://localhost:8080/actuator/health
4. **Api Docs**
    ```bash
   http://localhost:8080/swagger-ui/index.html

## ⚙️ Coverage Setup

Jacoco is used, and fail coverage when below 95% in each module

## 📋 Postman Collection

Import docs/eagle-bank.postman.json into Postman.
Variables:

{{base_url}} → http://localhost:8080
{{jwt_token}} → (set after login)
{{userId}} → (set after signup)

## 🔧 Common Tasks

Generate new build
./gradlew clean build
Run only user-controller tests
./gradlew :eagle-bank-app:test --tests *UserControllerTest
Open coverage report
open eagle-bank-app/build/jacocoHtml/index.html

## 🙋‍♂️ Contributing

Fork & clone
Create feature branch (git checkout -b feat/xyz)
Commit & PR
Ensure tests & coverage pass