# Service Definitions

## API Gateway (Port 8080)
The single entry point for all client requests. Routes traffic to the correct microservice based on URL path prefixes (`/api/auth/*` → Auth Service, `/api/patients/*` → Patient Service, `/api/sessions/*` and `/api/protocols/*` → Session & Protocol Service, etc.). Validates JWT tokens on every request before forwarding. Handles CORS. Stateless — no database.

---

## Auth Service (Port 8084)
Manages **who can access the system and what they can do**.

- User registration and login
- Password hashing (BCrypt)
- JWT token generation and validation
- Role assignment: ADMIN, THERAPIST, NURSE, RESEARCHER, COORDINATOR
- Each role maps to clinical responsibilities (e.g., THERAPIST can write session notes, NURSE can record vitals, RESEARCHER has read-only access to outcomes)

---

## Patient Service (Port 8081)
The **core patient record** — the central identity that other services reference.

- Demographics and contact info
- ICD-10 coded diagnoses (PTSD, MDD, Treatment-Resistant Depression)
- Current/past medications with washout tracking (critical because SSRIs/MAOIs must be tapered before psychedelic dosing)
- Contraindication flags
- Allergies and emergency contacts
- Therapist profiles — credentials, specialties, dyad pairing info (psychedelic therapy typically uses a male+female therapist pair)

---

## Session & Protocol Service (Port 8082)
Manages **treatment protocols and the session lifecycle**. Combines protocol definitions with session execution since sessions must constantly validate against protocol rules (dose limits, safety thresholds, phase requirements).

### Protocol Management
- Protocol definitions: substance, target condition, dosing parameters, session structure, safety thresholds
- Eligibility criteria (inclusion/exclusion rules)
- Contraindicated medications and required washout periods
- Patient enrollment tracking — which patients are in which protocol, what phase they're in
- Safety thresholds (e.g., max BP/HR/temp before intervention is required)

### Session Lifecycle
Manages four distinct treatment phases:

1. **Preparation** — rapport-building therapy sessions before medicine (2-3 sessions)
2. **Dosing** — the medicine session itself (6-8 hours)
3. **Integration** — post-session processing (3+ sessions)
4. **Follow-up** — long-term outcome tracking (up to 12 months)

Per session it tracks: scheduling, therapist notes (timestamped), vital signs (BP, HR, temp — critical during MDMA dosing), dose administration details (substance, amount, time, boosters), adverse events (severity, resolution), and set/setting documentation.

---

## Assessment Service (Port 8086)
Tracks **validated clinical outcome scales** — standardized questionnaires measuring symptom severity over time.

- Scale definitions: PCL-5 (PTSD), MADRS (depression), PHQ-9 (depression), BDI-II (depression), GAD-7 (anxiety)
- Score recording at specific timepoints (baseline, pre-dose, post-dose, follow-ups)
- Score trajectory tracking to show treatment efficacy
- Clinical cutoff awareness (e.g., PCL-5 >= 33 indicates probable PTSD)

---

## Notification Service (Port 8083)
Sends **therapy-workflow-specific reminders and alerts**.

- Session reminders (prep, dosing, integration appointments)
- Assessment due notices (scales administered at specific timepoints)
- Washout reminders (medication taper schedules)
- Safety alerts (vital sign threshold breaches during dosing)
- Integration check-ins (post-session follow-up prompts)
- Email delivery via SMTP (MailHog in dev)

---

## Frontend (Port 3000)
React + TypeScript SPA served via Nginx. Proxies all `/api/` requests to the API Gateway. Handles client-side routing for the clinical UI.
