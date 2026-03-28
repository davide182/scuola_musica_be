package com.scuoladimusica.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scuoladimusica.TestDataFactory;
import com.scuoladimusica.model.dto.request.StudentRequest;
import com.scuoladimusica.model.entity.*;
import com.scuoladimusica.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory dati;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // ========== CREAZIONE ==========

    @Nested
    @DisplayName("POST /api/students - Creazione studente")
    class CreazioneStudente {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin crea uno studente con successo e riceve 201 Created")
        void adminCreaStudente_201() throws Exception {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Mario", "Rossi",
                    LocalDate.of(1990, 1, 1), "3331234567", Livello.PRINCIPIANTE);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.matricola").value("M001"))
                    .andExpect(jsonPath("$.nomeCompleto").value("Mario Rossi"))
                    .andExpect(jsonPath("$.livello").value("PRINCIPIANTE"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin crea uno studente e il body contiene tutti i campi attesi")
        void adminCreaStudente_verificaBody() throws Exception {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Mario", "Rossi",
                    LocalDate.of(1990, 1, 1), "3331234567", Livello.PRINCIPIANTE);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.matricola").value("M001"))
                    .andExpect(jsonPath("$.cf").value("RSSMRA90A01H501Z"))
                    .andExpect(jsonPath("$.nome").value("Mario"))
                    .andExpect(jsonPath("$.cognome").value("Rossi"))
                    .andExpect(jsonPath("$.nomeCompleto").value("Mario Rossi"))
                    .andExpect(jsonPath("$.telefono").value("3331234567"))
                    .andExpect(jsonPath("$.livello").value("PRINCIPIANTE"));
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante crea uno studente con successo e riceve 201 Created")
        void insegnanteCreaStudente_201() throws Exception {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Mario", "Rossi",
                    LocalDate.of(1990, 1, 1), null, null);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Lo studente non puo' creare studenti e riceve 403 Forbidden")
        void studenteCreaStudente_403() throws Exception {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Mario", "Rossi",
                    LocalDate.of(1990, 1, 1), null, null);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Un utente non autenticato non puo' creare studenti e riceve 401 Unauthorized")
        void utenteNonAutenticato_401() throws Exception {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Mario", "Rossi",
                    LocalDate.of(1990, 1, 1), null, null);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("La creazione con matricola duplicata restituisce 409 Conflict")
        void matricolaDuplicata_409() throws Exception {
            dati.creaStudentePredefinito();

            StudentRequest request = new StudentRequest(
                    "M001", "ALTROCF123456789", "Altro", "Nome",
                    LocalDate.of(1985, 2, 2), null, null);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("La creazione con nome vuoto restituisce 400 Bad Request")
        void nomeVuoto_400() throws Exception {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "", "Rossi",
                    LocalDate.of(1990, 1, 1), null, null);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("La creazione con cognome vuoto restituisce 400 Bad Request")
        void cognomeVuoto_400() throws Exception {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Mario", "",
                    LocalDate.of(1990, 1, 1), null, null);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("La creazione con matricola vuota restituisce 400 Bad Request")
        void matricolaVuota_400() throws Exception {
            StudentRequest request = new StudentRequest(
                    "", "RSSMRA90A01H501Z", "Mario", "Rossi",
                    LocalDate.of(1990, 1, 1), null, null);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========== LETTURA ==========

    @Nested
    @DisplayName("GET /api/students - Recupero studenti")
    class RecuperoStudenti {

        @BeforeEach
        void preparaDati() {
            dati.creaStudentePredefinito();
            dati.creaSecondoStudente();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin recupera tutti gli studenti e riceve 200 OK")
        void adminRecuperaTutti_200() throws Exception {
            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin recupera tutti gli studenti e la lista non e' vuota")
        void adminRecuperaTutti_listaPopulata() throws Exception {
            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Lo studente non puo' vedere tutti gli studenti e riceve 403 Forbidden")
        void studenteRecuperaTutti_403() throws Exception {
            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isForbidden());
        }
    }

    // ========== RICERCA PER MATRICOLA ==========

    @Nested
    @DisplayName("GET /api/students/{matricola} - Ricerca per matricola")
    class RicercaPerMatricola {

        @BeforeEach
        void preparaDati() {
            dati.creaStudentePredefinito();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Lo studente viene trovato per matricola e riceve 200 OK")
        void studenteTrovato_200() throws Exception {
            mockMvc.perform(get("/api/students/M001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.matricola").value("M001"))
                    .andExpect(jsonPath("$.nomeCompleto").value("Mario Rossi"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("La risposta JSON contiene tutti i campi attesi dello studente")
        void verificaBodyCompleto() throws Exception {
            mockMvc.perform(get("/api/students/M001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.matricola").value("M001"))
                    .andExpect(jsonPath("$.cf").value("RSSMRA90A01H501Z"))
                    .andExpect(jsonPath("$.nome").value("Mario"))
                    .andExpect(jsonPath("$.cognome").value("Rossi"))
                    .andExpect(jsonPath("$.nomeCompleto").value("Mario Rossi"))
                    .andExpect(jsonPath("$.livello").value("PRINCIPIANTE"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Uno studente inesistente restituisce 404 Not Found")
        void studenteNonTrovato_404() throws Exception {
            mockMvc.perform(get("/api/students/INESISTENTE"))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== FILTRO PER LIVELLO ==========

    @Nested
    @DisplayName("GET /api/students/livello/{livello} - Filtro per livello")
    class FiltroPerLivello {

        @BeforeEach
        void preparaDati() {
            dati.creaStudentePredefinito();    // PRINCIPIANTE
            dati.creaSecondoStudente();        // INTERMEDIO
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Il filtro per livello PRINCIPIANTE restituisce solo gli studenti principianti")
        void filtroPrincipiante_200() throws Exception {
            mockMvc.perform(get("/api/students/livello/PRINCIPIANTE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].matricola").value("M001"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Il filtro per livello INTERMEDIO restituisce solo gli studenti intermedi")
        void filtroIntermedio_200() throws Exception {
            mockMvc.perform(get("/api/students/livello/INTERMEDIO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].matricola").value("M002"));
        }
    }

    // ========== AGGIORNAMENTO ==========

    @Nested
    @DisplayName("PUT /api/students/{matricola} - Aggiornamento studente")
    class AggiornamentoStudente {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin aggiorna uno studente con successo e riceve 200 OK")
        void adminAggiorna_200() throws Exception {
            dati.creaStudentePredefinito();

            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Marco", "Rossi",
                    LocalDate.of(1990, 1, 1), "3339876543", Livello.INTERMEDIO);

            mockMvc.perform(put("/api/students/M001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Marco"))
                    .andExpect(jsonPath("$.livello").value("INTERMEDIO"));
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante non puo' aggiornare uno studente e riceve 403 Forbidden")
        void insegnanteAggiorna_403() throws Exception {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Test", "Test",
                    LocalDate.of(1990, 1, 1), null, null);

            mockMvc.perform(put("/api/students/M001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'aggiornamento di uno studente inesistente restituisce 404 Not Found")
        void aggiornamentoNonTrovato_404() throws Exception {
            StudentRequest request = new StudentRequest(
                    "INESISTENTE", "RSSMRA90A01H501Z", "Test", "Test",
                    LocalDate.of(1990, 1, 1), null, null);

            mockMvc.perform(put("/api/students/INESISTENTE")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== ELIMINAZIONE ==========

    @Nested
    @DisplayName("DELETE /api/students/{matricola} - Eliminazione studente")
    class EliminazioneStudente {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin elimina uno studente con successo e riceve 204 No Content")
        void adminElimina_204() throws Exception {
            dati.creaStudentePredefinito();

            mockMvc.perform(delete("/api/students/M001"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Lo studente non puo' eliminare uno studente e riceve 403 Forbidden")
        void studenteElimina_403() throws Exception {
            mockMvc.perform(delete("/api/students/M001"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'eliminazione di uno studente inesistente restituisce 404 Not Found")
        void eliminazioneNonTrovato_404() throws Exception {
            mockMvc.perform(delete("/api/students/INESISTENTE"))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== REPORT ==========

    @Nested
    @DisplayName("GET /api/students/{matricola}/report - Report studente")
    class ReportStudente {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin ottiene il report di uno studente con corsi e voti e riceve 200 OK")
        void adminReportConCorsi_200() throws Exception {
            Student studente = dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            Course corso = dati.creaCorsoPredefinito(insegnante);
            dati.creaIscrizioneConVoto(studente, corso, 2026, 28);

            mockMvc.perform(get("/api/students/M001/report"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.studente").value("Mario Rossi"))
                    .andExpect(jsonPath("$.numCorsi").value(1))
                    .andExpect(jsonPath("$.mediaVoti").value(28.0))
                    .andExpect(jsonPath("$.corsi", hasSize(1)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Il report contiene il livello dello studente")
        void reportContienelivello() throws Exception {
            dati.creaStudentePredefinito();

            mockMvc.perform(get("/api/students/M001/report"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.livello").value("PRINCIPIANTE"));
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante puo' vedere il report di uno studente e riceve 200 OK")
        void insegnanteReport_200() throws Exception {
            dati.creaStudentePredefinito();

            mockMvc.perform(get("/api/students/M001/report"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.studente").value("Mario Rossi"));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Lo studente non puo' vedere il report e riceve 403 Forbidden")
        void studenteReport_403() throws Exception {
            mockMvc.perform(get("/api/students/M001/report"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Il report di uno studente inesistente restituisce 404 Not Found")
        void reportNonTrovato_404() throws Exception {
            mockMvc.perform(get("/api/students/INESISTENTE/report"))
                    .andExpect(status().isNotFound());
        }
    }
}