# Insurance Policy & Claims Management System (IPCMS)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.3.1-blue.svg)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-6.3.5-purple.svg)](https://vitejs.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)

A full-stack, enterprise-grade Insurance Policy and Claims Management System designed for managing underwriting procedures, policy issuance, endorsement workflows, premium calculation matrices, first notice of loss (FNOL) claims processing, gross reserve management, and AML regulatory compliance.

---

## Table of Contents
- [Architecture & Tech Stack](#architecture--tech-stack)
- [Key Features & Functional Requirements](#key-features--functional-requirements)
- [Seeded Test Accounts](#seeded-test-accounts)
- [Getting Started](#getting-started)
  - [Option A: Docker Compose (Recommended)](#option-a-docker-compose-recommended)
  - [Option B: Local Development](#option-b-local-development)
- [Postman API Collection](#postman-api-collection)
- [Testing & Quality Assurance](#testing--quality-assurance)
- [Repository Structure](#repository-structure)

---

## Architecture & Tech Stack

The system is built on a clean, layered Controller-Service-Repository architecture:

- **Frontend**: React 18 with Vite, Tailwind CSS, Lucide Icons, Radix UI primitives, Recharts.
- **Backend**: Spring Boot 3 (Java 17), Spring Security with JWT stateless authentication, Spring Data JPA, Hibernate, AspectJ AOP logging.
- **Database**: PostgreSQL 16 with ACID-compliant transaction boundaries.
- **DevOps / Containerization**: Multi-stage Docker builds, Docker Compose, Nginx reverse proxy.

---

## Key Features & Functional Requirements

| Requirement | Module | Description |
| :--- | :--- | :--- |
| **FR1 & FR7** | **Authentication & Onboarding** | JWT token authentication with role-based dashboard filtering. Policyholder self-registration with automated validation. |
| **FR2** | **Underwriting Risk Matrix** | Dynamic multi-variable risk scoring (Region, Asset Age, Prior Claims, Business Type), automated premium quote generation, and underwriter override request/sign-off pipeline. |
| **FR3** | **Claims & FNOL** | First Notice of Loss (FNOL) registration with strict 14-day incident cutoff validation, dynamic `CLM-YYYY-NNNN` generation, and claims gross reserve adjustment. |
| **FR4** | **Policy Endorsements** | Mid-term policy modifications with real-time pro-rata premium adjustments and immutable baseline snapshot archiving (`policy_history`). |
| **FR5** | **Async AML Screening** | Non-blocking Anti-Money Laundering watchlist scanning that flags high-risk accounts and locks user login access asynchronously. |
| **FR6** | **Audit Trail & Governance** | Immutable audit logs capturing every user lifecycle change, claim filing, reserve revision, and underwriting decision. |

---

## Seeded Test Accounts

All pre-seeded accounts use the password: **`password123`**

| Role | Email | Office / Region | Primary Capabilities |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `admin@ipcms.local` | Lagos | User registry management, account activation, system-wide audit logs, supervisor override sign-offs |
| **Underwriter** | `underwriter@ipcms.local` | London | Underwriting matrix, rate quote calculations, override submissions, policy endorsements |
| **Claims Handler** | `claims_handler@ipcms.local` | Stockholm | Claims registry, reserve adjustments, investigation tracking |
| **Insurance Agent** | `agent@ipcms.local` | Dakar | Portfolio management, FNOL claim registration on behalf of clients |
| **Standard Policyholder** | `holder@ipcms.local` | Singapore | Profile view, active policy tracking, FNOL claim filing |

*(Note: `policyholder@ipcms.local` is also seeded as an alias).*

---

## Getting Started

### Option A: Docker Compose (Recommended)

Run the entire stack (PostgreSQL, Spring Boot backend, React + Nginx frontend) with a single command from the project root:

```bash
docker compose up --build
```

- **Frontend Application**: [http://localhost:5173](http://localhost:5173) (or [http://localhost:80](http://localhost:80))
- **Backend REST API**: [http://localhost:8080](http://localhost:8080)
- **PostgreSQL Database**: `localhost:5432` (`insurance_db` / `postgres` / `Anchal@18`)

To stop the containers:
```bash
docker compose down
```

---

### Option B: Local Development

#### 1. PostgreSQL Database
Ensure PostgreSQL is running locally and create the database:
```sql
CREATE DATABASE insurance_db;
```
*(Default credentials in `application.properties`: user `postgres`, password `Anchal@18` on port `5432`).*

#### 2. Backend Service (Spring Boot 3)
```powershell
cd backend
mvn clean spring-boot:run
```
The backend starts on [http://localhost:8080](http://localhost:8080). Default users and initial policies are seeded automatically on first startup.

#### 3. Frontend Service (React + Vite)
```powershell
cd frontend
npm install
npm run dev
```
Open [http://localhost:5173](http://localhost:5173) in your browser.

---

## Postman API Collection

A fully configured Postman collection is included at [`postman/Insurance.postman_collection.json`](postman/Insurance.postman_collection.json).

It contains ready-to-use requests for:
1. **Authentication**: Login across all 5 roles, Logout (auto-populates `{{token}}` bearer header)
2. **Policies & Onboarding**: Self-registration, AML test cases, Policy creation, Endorsement, History snapshots
3. **Claims Management**: FNOL filing, Claim listings, Gross reserve updates
4. **Underwriting Matrix**: Rate quote calculations, Override request & approval workflow
5. **Administration**: User creation, Active status toggling, Audit log retrieval

---

## Testing & Quality Assurance

### Backend Automated Tests
Run unit and integration test suites:
```powershell
cd backend
mvn test
```
All service tests (UserService, PolicyService, ClaimService, UnderwriteService, AmlService, AuditLogService, JwtTokenProvider) run and pass with 100% success rate.

### Frontend Production Build
Validate TypeScript, CSS bundling, and production asset build:
```powershell
cd frontend
npm run build
```

### Manual Verification Guide
For step-by-step UI testing workflows for every scenario (A through G), refer to [`manual_test_guide.md`](manual_test_guide.md).

---

## Repository Structure

```text
Insurance-policy-claim-system/
├── backend/                   # Spring Boot 3 Java Backend
│   ├── src/main/java/         # Application source (AOP, Controller, Service, Repository, Security)
│   ├── src/main/resources/    # application.properties
│   └── src/test/              # JUnit 5 & Mockito test suites
├── frontend/                  # React + Vite Frontend
│   ├── src/app/               # React components, UI views, services
│   └── package.json           # Dependencies and scripts
├── database/                  # SQL scripts
│   ├── schema.sql             # Table DDL definitions
│   └── data.sql               # Seed data for testing
├── docker/                    # Docker configuration
│   ├── Dockerfile.backend     # Multi-stage Maven + Temurin build
│   ├── Dockerfile.frontend    # Multi-stage Node + Nginx build
│   ├── nginx.conf             # Nginx SPA & reverse proxy config
│   └── docker-compose.yml     # Docker Compose definition
├── docs/                      # Project documentation and specifications
│   ├── project_context.md     # Architecture overview
│   ├── project_rules.md       # Implementation rules
│   └── Database_Documentation_IPCMS.docx
├── postman/                   # API testing collections
│   └── Insurance.postman_collection.json
├── docker-compose.yml         # Root Docker Compose runner
├── manual_test_guide.md       # Comprehensive functional testing guide
└── README.md                  # Project documentation
```
