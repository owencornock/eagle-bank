# 🦅 Eagle Bank

A full-stack banking application built with Spring Boot and React, providing comprehensive banking services including user management, account operations, and transaction handling.

---

## 🚀 Features

- **Users**
    - Sign up, login, profile management
    - JWT-based authentication
- **Accounts**
    - Create, list, fetch, update, delete
    - Multiple account support per user
- **Transactions**
    - Deposit & withdrawal operations
    - Transaction history tracking
- **Modern UI**
    - Responsive React frontend
    - Real-time updates
    - Secure authentication flow
- **API Documentation**
    - OpenAPI (Swagger UI)
    - Postman collection

---

## 🛠 Prerequisites

- Backend:
    - Java 21 (via SDKMAN / homebrew)
    - Gradle (wrapper included)
- Frontend:
    - Node.js 20+
    - pnpm (preferred package manager)
- Optional:
    - Docker & Docker Compose for containerization
    - PostgreSQL (if not using Docker)

---

## ⚙️ Backend Setup

1. **Configure**
   ```bash
   # Copy and configure application.yml
   cp eagle-bank-app/src/main/resources/application.yml.example eagle-bank-app/src/main/resources/application.yml
   ```

2. **Build & Run**
   ```bash
   ./gradlew clean build
   ./gradlew :eagle-bank-app:bootRun
   ```

## 🎨 Frontend Setup

1. **Install Dependencies**
   ```bash
   cd eagle-bank-frontend
   pnpm install
   ```

2. **Configure Environment**
   ```bash
   cp .env.example .env
   ```

3. **Run Development Server**
   ```bash
   pnpm dev
   ```

4. **Access Application**
   ```
   http://localhost:5173
   ```

## 🐳 Docker Setup

Build and run both frontend and backend:

## 📚 Documentation

- **API Docs**: http://localhost:8080/swagger-ui/index.html
- **Postman**: Import `docs/eagle-bank.postman.json`
    - Variables:
        - `{{base_url}}` → http://localhost:8080
        - `{{jwt_token}}` → (set after login)
        - `{{userId}}` → (set after signup)

## ⚡ Development

### Code Coverage
- Jacoco is configured for backend code coverage
- Minimum 95% coverage required per module
- View report: `open eagle-bank-app/build/jacocoHtml/index.html`

### Common Commands

## 🔒 Security

- JWT-based authentication
- CORS configuration for local development
- Secure password hashing
- Protected API endpoints

## 🤝 Contributing

1. Fork & clone the repository
2. Create feature branch (`git checkout -b feat/xyz`)
3. Commit changes & push
4. Open Pull Request
5. Ensure all tests pass and coverage requirements are met

## 🚢 Kubernetes Deployment

### Prerequisites
- Kubernetes cluster (local or cloud)
- kubectl CLI tool
- Docker
- Minikube (for local development)

### Local Deployment

1. **Start Minikube** (if using local cluster)
   ```bash
   minikube start
   ```

2. **Deploy to Kubernetes**
   ```bash
   # Make scripts executable
   chmod +x deploy.sh cleanup.sh

   # Deploy the application
   ./deploy.sh
   ```

   This script will:
   - Build Docker images for both frontend and backend
   - Apply Kubernetes configurations
   - Wait for pods to be ready
   - Display service URLs

3. **Access the Application**
   - Frontend: http://localhost
   - Backend API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui/index.html

4. **View Deployment Status**
   ```bash
   kubectl get pods -n eagle-bank-local
   kubectl get services -n eagle-bank-local
   ```

### Cleanup

To remove all deployed resources:
```bash
./cleanup.sh
```