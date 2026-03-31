# Scuola di Musica - Backend API

Sistema gestionale per una scuola di musica. API REST sviluppata con Spring Boot per la gestione di studenti, insegnanti, corsi, strumenti musicali e iscrizioni. Autenticazione JWT con controllo accessi basato su ruoli. Integrazione Jenkins per automazione build e test.

---

## Indice

- [Tecnologie](#tecnologie)
- [Architettura](#architettura)
- [Funzionalità](#funzionalità)
- [Sicurezza](#sicurezza)
- [Struttura del progetto](#struttura-del-progetto)
- [Configurazione](#configurazione)
- [Avvio dell'applicazione](#avvio-dellapplicazione)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [CI/CD con Jenkins](#cicd-con-jenkins)
- [Modello dei dati](#modello-dei-dati)

---

## Tecnologie

### Backend

| Tecnologia | Versione | Utilizzo |
|------------|----------|----------|
| Java | 21 | Linguaggio principale |
| Spring Boot | 3.4.3 | Framework applicativo |
| Spring Security | 6.x | Autenticazione e autorizzazione |
| Spring Data JPA | 3.x | ORM e accesso al database |
| Spring Web | 3.x | API REST |
| Spring Validation | 3.x | Validazione input |
| JJWT | 0.11.5 | Generazione e validazione token JWT |
| Lombok | 1.18.34 | Riduzione codice boilerplate |

### Database

| Tecnologia | Utilizzo |
|------------|----------|
| H2 Database | Database in-memory per sviluppo e test |
| Hibernate | ORM / generazione schema |

### Documentazione API

| Tecnologia | Versione | Utilizzo |
|------------|----------|----------|
| Springdoc OpenAPI | 2.1.0 | Generazione documentazione Swagger |

### Build & Test

| Tecnologia | Utilizzo |
|------------|----------|
| Maven | Build system e gestione dipendenze |
| JUnit 5 | Framework di testing |
| MockMvc | Test degli endpoint HTTP |
| Spring Security Test | Test con autenticazione simulata |

### CI/CD

| Tecnologia | Utilizzo |
|------------|----------|
| Jenkins | Automazione build, test e avvio applicazione |

---

## Architettura

L'applicazione segue un'architettura a livelli di Spring Boot:
Request HTTP
│
▼
┌─────────────────────────────────────┐
│ Security Filter Chain │ ── JWT validation, autenticazione
└─────────────────────────────────────┘
│
▼
┌─────────────────────────────────────┐
│ Controller Layer │ ── REST endpoints, @PreAuthorize
└─────────────────────────────────────┘
│
▼
┌─────────────────────────────────────┐
│ Service Layer │ ── Business logic, transazioni
└─────────────────────────────────────┘
│
▼
┌─────────────────────────────────────┐
│ Repository Layer │ ── Spring Data JPA
└─────────────────────────────────────┘
│
▼
┌─────────────────────────────────────┐
│ H2 In-Memory Database │
└─────────────────────────────────────┘

### Pattern utilizzati

- **DTO Pattern** — Separazione tra oggetti di request/response e entity JPA
- **Service-Repository Pattern** — Logica di business separata dall'accesso ai dati
- **Global Exception Handling** — Gestione centralizzata degli errori
- **Stateless JWT Authentication** — Nessuna sessione server-side

---

## Funzionalità

### Autenticazione e Registrazione
- Registrazione utente con assegnazione ruolo
- Login con generazione token JWT (HS512, scadenza 24 ore)
- Protezione endpoint tramite `@PreAuthorize` con verifica ruolo

### Gestione Studenti
- CRUD completo per studenti
- Campi: matricola, codice fiscale, nome, cognome, data nascita, telefono
- Livelli: PRINCIPIANTE, INTERMEDIO, AVANZATO
- Ricerca per matricola e per livello
- Report dettagliato con media voti e corsi frequentati

### Gestione Insegnanti
- CRUD completo per insegnanti
- Campi: matricola, codice fiscale, nome, cognome, data nascita, stipendio, specializzazione, anni esperienza
- Assegnazione corsi agli insegnanti

### Gestione Corsi
- CRUD completo per corsi
- Campi: codice corso, nome, date inizio/fine, costo orario, totale ore, modalità online, livello
- Associazione con insegnante responsabile
- Calcolo automatico costo totale e durata giorni

### Iscrizioni
- Iscrizione studente a corso con anno di iscrizione
- Registrazione voto finale (scala 18-30)
- Visualizzazione iscrizioni per studente o per corso
- Calcolo automatico media voti per studente

### Gestione Strumenti
- CRUD completo per strumenti musicali
- Tipi: TASTIERA, CORDA, ARCO, PERCUSSIONE, FIATO
- Attributi specifici per tipologia (numero corde, tipo pelle, diametro)
- Gestione prestiti con verifica disponibilità

### Lezioni
- Associazione lezioni a corsi
- Campi: numero progressivo, data, ora inizio, durata, aula, argomento
- Vincolo univoco per numero lezione all'interno del corso

---

## Sicurezza

### Ruoli

| Ruolo | Accesso |
|-------|---------|
| ROLE_ADMIN | Accesso completo a tutte le operazioni |
| ROLE_TEACHER | Gestione corsi, lezioni, voti e prestiti strumenti |
| ROLE_STUDENT | Visualizzazione corsi, gestione iscrizioni proprie |

### Flusso di autenticazione JWT
POST /api/auth/signup → Registrazione utente

POST /api/auth/signin → Login → Risposta con JWT token

Tutte le richieste successive includono l'header:
Authorization: Bearer <token>

AuthTokenFilter valida il token ad ogni richiesta

SecurityContext viene popolato con i dati dell'utente

### Endpoint pubblici

| Endpoint | Descrizione |
|----------|-------------|
| `/api/auth/**` | Registrazione e login |
| `/h2-console/**` | Console H2 (solo sviluppo) |
| `/swagger-ui/**` | Documentazione API |
| `/api-docs/**` | Schema OpenAPI |

### Configurazione sicurezza

- Password hashing con BCrypt
- Sessione STATELESS (nessuna HttpSession)
- CSRF disabilitato per API REST
- Frame options disabilitate per console H2

---

## Struttura del progetto

src/
├── main/
│ ├── java/com/scuoladimusica/
│ │ ├── ScuolaDiMusicaApplication.java
│ │ ├── config/
│ │ │ ├── DataLoader.java
│ │ │ └── SecurityConfig.java
│ │ ├── controller/
│ │ │ ├── AuthController.java
│ │ │ ├── CourseController.java
│ │ │ ├── EnrollmentController.java
│ │ │ ├── InstrumentController.java
│ │ │ ├── StudentController.java
│ │ │ └── TeacherController.java
│ │ ├── exception/
│ │ │ ├── BusinessRuleException.java
│ │ │ ├── DuplicateResourceException.java
│ │ │ ├── GlobalExceptionHandler.java
│ │ │ └── ResourceNotFoundException.java
│ │ ├── model/
│ │ │ ├── dto/
│ │ │ │ ├── request/
│ │ │ │ └── response/
│ │ │ └── entity/
│ │ ├── repository/
│ │ ├── security/
│ │ │ ├── jwt/
│ │ │ │ ├── AuthEntryPointJwt.java
│ │ │ │ ├── AuthTokenFilter.java
│ │ │ │ └── JwtUtils.java
│ │ │ └── services/
│ │ │ ├── UserDetailsImpl.java
│ │ │ └── UserDetailsServiceImpl.java
│ │ └── service/
│ └── resources/
│ └── application.properties
└── test/
├── java/com/scuoladimusica/
│ ├── TestDataFactory.java
│ ├── controller/
│ └── service/
└── resources/
└── application-test.properties

## Configurazione

### application.properties (sviluppo)

```properties
# Database H2 in-memory
spring.datasource.url=jdbc:h2:mem:scuola_musica
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=admin
spring.datasource.password=admin
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JWT
myapp.jwtSecret=<chiave_segreta_minimo_64_caratteri>
myapp.jwtExpirationMs=86400000

# Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
Avvio dell'applicazione
Prerequisiti
Java 21+

Maven 3.8+

API Endpoints

Autenticazione

Metodo	Endpoint	Descrizione	Accesso
POST	/api/auth/signup	Registrazione nuovo utente	Pubblico
POST	/api/auth/signin	Login e ottenimento token JWT	Pubblico

Studenti

Metodo	Endpoint	Descrizione	Ruolo richiesto
GET	/api/students	Lista tutti gli studenti	ADMIN, TEACHER
GET	/api/students/{matricola}	Dettaglio studente	Autenticato
POST	/api/students	Crea nuovo studente	ADMIN, TEACHER
PUT	/api/students/{matricola}	Aggiorna studente	ADMIN
DELETE	/api/students/{matricola}	Elimina studente	ADMIN
GET	/api/students/{matricola}/report	Report studente con media voti	ADMIN, TEACHER
GET	/api/students/livello/{livello}	Filtra studenti per livello	ADMIN, TEACHER

Insegnanti

Metodo	Endpoint	Descrizione	Ruolo richiesto
GET	/api/teachers	Lista tutti gli insegnanti	ADMIN
GET	/api/teachers/{matricola}	Dettaglio insegnante	ADMIN, TEACHER
POST	/api/teachers	Crea nuovo insegnante	ADMIN
PUT	/api/teachers/{matricola}	Aggiorna insegnante	ADMIN
DELETE	/api/teachers/{matricola}	Elimina insegnante	ADMIN
POST	/api/teachers/{matricola}/courses/{codiceCorso}	Assegna corso a insegnante	ADMIN

Corsi

Metodo	Endpoint	Descrizione	Ruolo richiesto
GET	/api/courses	Lista tutti i corsi	Autenticato
GET	/api/courses/online	Lista corsi online	Autenticato
GET	/api/courses/{codiceCorso}	Dettaglio corso	Autenticato
POST	/api/courses	Crea nuovo corso	ADMIN
PUT	/api/courses/{codiceCorso}	Aggiorna corso	ADMIN, TEACHER
DELETE	/api/courses/{codiceCorso}	Elimina corso	ADMIN
POST	/api/courses/{codiceCorso}/lessons	Aggiunge lezione al corso	ADMIN, TEACHER
POST	/api/courses/{codiceCorso}/instruments/{codiceStrumento}	Associa strumento al corso	ADMIN

Iscrizioni

Metodo	Endpoint	Descrizione	Ruolo richiesto
POST	/api/enrollments	Iscrive studente a corso	Autenticato
POST	/api/enrollments/{matricola}/{codiceCorso}/vote	Registra voto	ADMIN, TEACHER
GET	/api/enrollments/student/{matricola}	Iscrizioni per studente	ADMIN, TEACHER
GET	/api/enrollments/course/{codiceCorso}	Iscrizioni per corso	ADMIN, TEACHER

Strumenti

Metodo	Endpoint	Descrizione	Ruolo richiesto
GET	/api/instruments	Lista tutti gli strumenti	Autenticato
GET	/api/instruments/available	Lista strumenti disponibili	Autenticato
GET	/api/instruments/{codiceStrumento}	Dettaglio strumento	Autenticato
POST	/api/instruments	Aggiunge strumento	ADMIN
POST	/api/instruments/{codiceStrumento}/loan	Presta strumento a studente	ADMIN, TEACHER
POST	/api/instruments/{codiceStrumento}/return	Restituisce strumento	ADMIN, TEACHER


Testing

Esecuzione dei test

# Esegui tutti i test
mvn test

@WithMockUser — Simula utente autenticato con ruolo specifico

@Nested + @DisplayName — Organizzazione leggibile dei test

MockMvc — Simulazione richieste HTTP

@Transactional — Rollback automatico dopo ogni test

Database H2 dedicato per i test

CI/CD con Jenkins
Configurazione Jenkins
Parametro	Valore
Job	scuola-di-musica
Repository	https://github.com/davide182/scuola_musica_be.git
Branch	main
Trigger	Polling SCM (controllo ogni 5 minuti)
Build Step	mvn clean spring-boot:run
Statistiche
Test totali: 248

Build automatici: attivati a ogni push su GitHub

Tomcat: avviato

Modello dei dati

User ──────────────── Role
 │         (M:N)
 │
 ├──► Student ──────► Enrollment ◄──── Course ◄──── Teacher
 │                                        │
 │                                        └──────► Lesson
 │
 └──► (tramite prestito)
         Loan ◄──────────────────────────── Instrument

Entity principali
Entity	Campi chiave
User	username, email, password (BCrypt)
Role	name (ROLE_STUDENT, ROLE_TEACHER, ROLE_ADMIN)
Student	matricola, cf, nome, cognome, dataNascita, telefono, livello
Teacher	matricolaInsegnante, cf, specializzazione, stipendio, anniEsperienza
Course	codiceCorso, dataInizio, dataFine, costoOrario, totaleOre, online, livello
Enrollment	annoIscrizione, votoFinale (18-30)
Instrument	codiceStrumento, tipoStrumento, marca, annoProduzione
Loan	dataInizio, dataFine (null = prestito attivo)
Lesson	numero, data, oraInizio, durata, aula, argomento