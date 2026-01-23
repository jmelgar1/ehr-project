# Psychedelic Therapy EHR Application Plan

## Tech Stack
- **Frontend:** React + TypeScript
- **Backend:** Spring Boot + Java 21 (Microservices)
- **Database:** PostgreSQL 18.1 (database-per-service)
- **Infrastructure:** Docker + Docker Compose

---

## Overview

A psychedelic-therapy-focused Electronic Health Records (EHR) system with Clinical Trial Management (CTMS) elements. Supports MDMA-assisted therapy for PTSD, psilocybin-assisted therapy for depression, and is extensible to other protocols (ketamine, LSD, etc.).

---

## Microservice Architecture

### Services Overview

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| API Gateway | 8080 | - | Single entry point, routes requests, JWT validation |
| Auth Service | 8084 | ehr_auth_db | Users, roles, login, JWT token generation |
| Patient Service | 8081 | ehr_patient_db | Patients, diagnoses, medications, therapists |
| Session & Protocol Service | 8082 | ehr_session_db | Protocols, enrollment, treatment sessions, vitals, notes, adverse events |
| Assessment Service | 8086 | ehr_assessment_db | Clinical scales, score tracking |
| Notification Service | 8083 | ehr_notification_db | Alerts, reminders, communications |

---

## Service Definitions

### API Gateway (Port 8080)
- Single entry point for all frontend requests
- Routes requests to appropriate microservices based on URL paths
- Validates JWT tokens before forwarding requests
- Handles CORS configuration for frontend access
- No database - stateless routing layer

### Auth Service (Port 8084)
Manages user accounts and authentication. Handles login, JWT generation, and token validation.

**User Roles:**
| Role | Description |
|------|-------------|
| ADMIN | Full system access, manage users, system settings |
| THERAPIST | Lead or co-therapist in treatment sessions |
| NURSE | Vital sign monitoring, medication management |
| RESEARCHER | Read access for outcome data and reporting |
| COORDINATOR | Scheduling, enrollment management |

### Patient Service (Port 8081)
The core patient record system. Manages patient demographics, contact info, and clinical context.

**Key concepts:**
- **Diagnoses** — ICD-10 coded conditions (PTSD, MDD, Treatment-Resistant Depression)
- **Medications** — current/past medications with washout tracking (SSRIs, MAOIs need tapering before psychedelic treatment)
- **Contraindication awareness** — flags medications that conflict with specific protocols
- **Allergies and emergency contacts**
- **Therapist profiles** — credentials, specialties, and dyad pairing info (psychedelic therapy typically uses male+female therapist pairs)

### Session & Protocol Service (Port 8082)
Combines protocol definitions with session execution. Sessions must constantly validate against protocol rules (dose limits, safety thresholds, phase requirements), so co-locating them avoids synchronous inter-service calls on the critical dosing path.

**Protocol Management:**
- Which substance/formulation is used
- Target condition and eligibility criteria (inclusion/exclusion)
- Dosing parameters (initial dose, supplemental dose window)
- Session structure (how many prep/dosing/integration/follow-up sessions)
- Safety thresholds (max BP, HR, temperature before intervention)
- Contraindicated medications and required washout periods
- Time between dosing sessions
- Patient enrollment tracking (which patients are in which protocols, current phase/progress)

**Session Lifecycle:**
Psychedelic therapy follows a distinct phase model:

- **Preparation** — 2-3 rapport-building therapy sessions before medicine
- **Dosing** — the medicine session itself (6-8 hours, requires vital sign monitoring, therapist dyad present)
- **Integration** — 3+ post-session processing sessions
- **Follow-up** — long-term outcome tracking (up to 12 months)

Each session tracks:
- Scheduling and attendance
- Timestamped therapist notes
- Vital signs during dosing (BP, HR, temperature — critical for safety with MDMA)
- Dose administration details (substance, amount, time, supplemental doses)
- Adverse events (severity, category, resolution)
- Set and setting documentation (environment, music, comfort items)

### Assessment Service (Port 8086)
Manages validated clinical outcome scales — standardized questionnaires used to measure symptom severity over time:

| Scale | Target | Items | Range | Clinical Cutoff |
|-------|--------|-------|-------|-----------------|
| PCL-5 | PTSD | 20 | 0-80 | >= 33 |
| MADRS | Depression | 10 | 0-60 | >= 20 |
| PHQ-9 | Depression | 9 | 0-27 | >= 10 |
| BDI-II | Depression | 21 | 0-63 | >= 20 |
| GAD-7 | Anxiety | 7 | 0-21 | >= 10 |

Tracks score trajectories over treatment timepoints (baseline, pre/post-dose, follow-ups) to show treatment efficacy.

### Notification Service (Port 8083)
Sends reminders and alerts relevant to psychedelic therapy workflows:

- Session reminders (preparation, dosing, integration appointments)
- Assessment due notices (scales need to be administered at specific timepoints)
- Washout reminders (medication taper schedule alerts)
- Safety alerts (vital sign threshold breaches during dosing)
- Integration check-ins (post-session follow-up)
- Integrates with SMTP (MailHog in dev)

---

## Authentication & Security

### Auth Strategy: JWT + Spring Security

**Login Flow:**
1. User submits credentials to `/api/auth/login`
2. Auth Service validates credentials
3. Auth Service returns JWT token
4. Client stores token and sends in `Authorization: Bearer <token>` header
5. API Gateway validates token on each request
6. Gateway passes user info to downstream services

### Security Configuration
- Password hashing with BCrypt
- JWT tokens with configurable expiration
- API Gateway validates JWT signature before routing
- Public endpoints: `/api/auth/login`, `/api/auth/register`
- Protected endpoints: everything else requires valid JWT

---

## Architecture Decisions
- **Database-per-service pattern** — true microservice isolation
- **Spring Cloud Gateway** — API gateway with JWT validation filter
- **OpenFeign** — inter-service communication
- **Each service is independently deployable**
- **Docker Compose** — local development orchestration
- **UUID primary keys** — cross-service referencing without collisions

### Service Dependencies
```
session-service -> patient-service (get patient info, eligibility checks)
assessment-service -> patient-service (get patient info)
notification-service -> patient-service (get contact info)
notification-service -> session-service (get session schedule)
```

---

## Docker Configuration

### Infrastructure (docker-compose.yml)
- 5 PostgreSQL databases (one per domain service)
- MailHog for testing emails locally
- pgAdmin for database management

### Services (docker-compose.services.yml)
- 6 microservices (API Gateway, Auth, Patient, Session & Protocol, Assessment, Notification)
- React frontend with Nginx

### Development Workflow
- Run only databases in Docker during development
- Run services locally for hot reload capability

---

## Port Summary

| Component | Port |
|-----------|------|
| API Gateway | 8080 |
| Patient Service | 8081 |
| Session & Protocol Service | 8082 |
| Notification Service | 8083 |
| Auth Service | 8084 |
| Assessment Service | 8086 |
| PostgreSQL (Patient DB) | 5433 |
| PostgreSQL (Session DB) | 5434 |
| PostgreSQL (Notification DB) | 5435 |
| PostgreSQL (Auth DB) | 5436 |
| PostgreSQL (Assessment DB) | 5438 |
| MailHog SMTP | 1025 |
| MailHog Web UI | 8025 |
| pgAdmin | 5050 |
| React Frontend | 3000 |

---

## Implementation Order

1. Infrastructure — Docker configs, service directories
2. Auth Service — user accounts, clinical roles, JWT
3. Patient Service — patients, diagnoses, medications, therapists
4. Session & Protocol Service — protocols, enrollment, treatment sessions, notes, vitals, adverse events
6. Assessment Service — scale definitions, score tracking
7. Notification Service — therapy-specific notifications
8. API Gateway — route configuration
9. Frontend — React pages and components
