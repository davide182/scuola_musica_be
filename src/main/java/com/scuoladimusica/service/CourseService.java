package com.scuoladimusica.service;

import com.scuoladimusica.exception.BusinessRuleException;
import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.request.CourseRequest;
import com.scuoladimusica.model.dto.request.LessonRequest;
import com.scuoladimusica.model.dto.response.CourseResponse;
import com.scuoladimusica.model.dto.response.LessonResponse;
import com.scuoladimusica.model.entity.Course;
import com.scuoladimusica.model.entity.Instrument;
import com.scuoladimusica.model.entity.Lesson;
import com.scuoladimusica.model.entity.Livello;
import com.scuoladimusica.repository.CourseRepository;
import com.scuoladimusica.repository.InstrumentRepository;
import com.scuoladimusica.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    public CourseResponse createCourse(CourseRequest request) {
        if (courseRepository.existsByCodiceCorso(request.codiceCorso())) {
            throw new DuplicateResourceException("Corso già esistente con codice: " + request.codiceCorso());
        }
        
        if (request.dataFine().isBefore(request.dataInizio()) || request.dataFine().isEqual(request.dataInizio())) {
            throw new BusinessRuleException("La data di fine deve essere successiva alla data di inizio");
        }
        
        Livello livello = request.livello() != null ? request.livello() : Livello.PRINCIPIANTE;
        
        Course course = Course.builder()
                .codiceCorso(request.codiceCorso())
                .nome(request.nome())
                .dataInizio(request.dataInizio())
                .dataFine(request.dataFine())
                .costoOrario(request.costoOrario())
                .totaleOre(request.totaleOre())
                .online(request.online())
                .livello(livello)
                .build();
        
        Course saved = courseRepository.save(course);
        
        return mapToCourseResponse(saved);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseByCode(String codiceCorso) {
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));
        
        return mapToCourseResponse(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        List<CourseResponse> responses = new ArrayList<>();
        
        for (Course course : courses) {
            responses.add(mapToCourseResponse(course));
        }
        
        return responses;
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getOnlineCourses() {
        List<Course> courses = courseRepository.findByOnlineTrue();
        List<CourseResponse> responses = new ArrayList<>();
        
        for (Course course : courses) {
            responses.add(mapToCourseResponse(course));
        }
        
        return responses;
    }

    public CourseResponse updateCourse(String codiceCorso, CourseRequest request) {
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));
        
        if (request.dataFine().isBefore(request.dataInizio()) || request.dataFine().isEqual(request.dataInizio())) {
            throw new BusinessRuleException("La data di fine deve essere successiva alla data di inizio");
        }
        
        course.setNome(request.nome());
        course.setDataInizio(request.dataInizio());
        course.setDataFine(request.dataFine());
        course.setCostoOrario(request.costoOrario());
        course.setTotaleOre(request.totaleOre());
        course.setOnline(request.online());
        
        if (request.livello() != null) {
            course.setLivello(request.livello());
        }
        
        Course updated = courseRepository.save(course);
        
        return mapToCourseResponse(updated);
    }

    public void deleteCourse(String codiceCorso) {
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));
        
        courseRepository.delete(course);
    }

    public LessonResponse addLesson(String codiceCorso, LessonRequest request) {
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));
        
        if (lessonRepository.existsByCourseIdAndNumero(course.getId(), request.numero())) {
            throw new DuplicateResourceException("Lezione numero " + request.numero() + " già esistente per questo corso");
        }
        
        Lesson lesson = Lesson.builder()
                .course(course)
                .numero(request.numero())
                .data(request.data())
                .oraInizio(request.oraInizio())
                .durata(request.durata())
                .aula(request.aula())
                .argomento(request.argomento())
                .build();
        
        Lesson saved = lessonRepository.save(lesson);
        
        // IMPORTANTE: Aggiungi la lezione alla lista del corso per mantenere la coerenza
        course.getLessons().add(saved);
        
        return new LessonResponse(
                saved.getId(),
                saved.getNumero(),
                saved.getData(),
                saved.getOraInizio(),
                saved.getDurata(),
                saved.getAula(),
                saved.getArgomento()
        );
    }

    public void addInstrumentToCourse(String codiceCorso, String codiceStrumento) {
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));
        
        Instrument instrument = instrumentRepository.findByCodiceStrumento(codiceStrumento)
                .orElseThrow(() -> new ResourceNotFoundException("Strumento non trovato con codice: " + codiceStrumento));
        
        if (course.getInstruments().contains(instrument)) {
            throw new DuplicateResourceException("Strumento già associato a questo corso");
        }
        
        course.getInstruments().add(instrument);
        courseRepository.save(course);
    }
    
    private CourseResponse mapToCourseResponse(Course course) {
        // Carica le lezioni dal repository per assicurarsi di avere dati aggiornati
        List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
        
        List<LessonResponse> lessonResponses = new ArrayList<>();
        for (Lesson lesson : lessons) {
            lessonResponses.add(new LessonResponse(
                    lesson.getId(),
                    lesson.getNumero(),
                    lesson.getData(),
                    lesson.getOraInizio(),
                    lesson.getDurata(),
                    lesson.getAula(),
                    lesson.getArgomento()
            ));
        }
        
        String nomeInsegnante = course.getTeacher() != null ? 
                course.getTeacher().getNomeCompleto() : null;
        
        // Carica gli iscritti dal repository
        int numeroIscritti = course.getEnrollments() != null ? course.getEnrollments().size() : 0;
        
        return new CourseResponse(
                course.getId(),
                course.getCodiceCorso(),
                course.getNome(),
                course.getDataInizio(),
                course.getDataFine(),
                course.getCostoOrario(),
                course.getTotaleOre(),
                course.getCostoTotale(),
                course.getDurataGiorni(),
                course.isOnline(),
                course.getLivello(),
                nomeInsegnante,
                numeroIscritti,
                lessonResponses
        );
    }
}