package com.scuoladimusica.service;

import com.scuoladimusica.exception.BusinessRuleException;
import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.request.TeacherRequest;
import com.scuoladimusica.model.dto.response.TeacherResponse;
import com.scuoladimusica.model.entity.Course;
import com.scuoladimusica.model.entity.Teacher;
import com.scuoladimusica.repository.CourseRepository;
import com.scuoladimusica.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    public TeacherResponse createTeacher(TeacherRequest request) {
        if (teacherRepository.existsByMatricolaInsegnante(request.matricolaInsegnante())) {
            throw new DuplicateResourceException("Insegnante già esistente con matricola: " + request.matricolaInsegnante());
        }
        if (teacherRepository.existsByCf(request.cf())) {
            throw new DuplicateResourceException("Insegnante già esistente con codice fiscale: " + request.cf());
        }
        if (request.stipendio() <= 0) {
            throw new BusinessRuleException("Lo stipendio deve essere maggiore di 0");
        }
        
        Teacher teacher = Teacher.builder()
                .matricolaInsegnante(request.matricolaInsegnante())
                .cf(request.cf())
                .nome(request.nome())
                .cognome(request.cognome())
                .dataNascita(request.dataNascita())
                .telefono(request.telefono())
                .stipendio(request.stipendio())
                .specializzazione(request.specializzazione())
                .anniEsperienza(request.anniEsperienza())
                .build();
        
        Teacher teacherSave = teacherRepository.save(teacher);
        
        return mapToTeacherResponse(teacherSave);
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacherByMatricola(String matricola) {
        Teacher teacher = teacherRepository.findByMatricolaInsegnante(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con matricola: " + matricola));
        
        return mapToTeacherResponse(teacher);
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAllTeachers() {
        List<Teacher> teachers = teacherRepository.findAll();
        List<TeacherResponse> teacherAll = new ArrayList<>();
        
        // CORRETTO: restituisci lista vuota invece di lanciare eccezione
        for (Teacher teacher : teachers) {
            teacherAll.add(mapToTeacherResponse(teacher));
        }
        return teacherAll;
    }

    public TeacherResponse updateTeacher(String matricola, TeacherRequest request) {
        Teacher teacher = teacherRepository.findByMatricolaInsegnante(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con matricola : " + matricola));

        teacher.setNome(request.nome());
        teacher.setCognome(request.cognome());
        teacher.setTelefono(request.telefono());
        teacher.setStipendio(request.stipendio());
        teacher.setSpecializzazione(request.specializzazione());
        teacher.setAnniEsperienza(request.anniEsperienza());

        Teacher saved = teacherRepository.save(teacher);
        
        return mapToTeacherResponse(saved);
    }

    public void deleteTeacher(String matricola) {
        Teacher teacher = teacherRepository.findByMatricolaInsegnante(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con matricola : " + matricola));
        teacherRepository.delete(teacher);
    }

    public void assignCourse(String matricolaInsegnante, String codiceCorso) {
        Teacher teacher = teacherRepository.findByMatricolaInsegnante(matricolaInsegnante)
                .orElseThrow(() -> new ResourceNotFoundException("Insegnante non trovato con matricola : " + matricolaInsegnante));
        
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice corso : " + codiceCorso));

        if (course.getTeacher() != null) {
            throw new BusinessRuleException("Corso già assegnato con codice corso : " + codiceCorso);
        }

        // Assegna il corso all'insegnante
        course.setTeacher(teacher);
        courseRepository.save(course);
        
        // IMPORTANTE: Aggiungi il corso alla lista dei corsi dell'insegnante
        // per mantenere la coerenza delle relazioni bidirezionali
        teacher.getCourses().add(course);
        teacherRepository.save(teacher);
    }
    
    private TeacherResponse mapToTeacherResponse(Teacher teacher) {
        // Carica i corsi dell'insegnante dal database
        List<Course> courses = courseRepository.findByTeacherId(teacher.getId());
        int numeroCorsi = courses != null ? courses.size() : 0;
        
        return new TeacherResponse(
                teacher.getId(),
                teacher.getMatricolaInsegnante(),
                teacher.getCf(),
                teacher.getNome(),
                teacher.getCognome(),
                teacher.getNomeCompleto(),
                teacher.getDataNascita(),
                teacher.getTelefono(),
                teacher.getStipendio(),
                teacher.getSpecializzazione(),
                teacher.getAnniEsperienza(),
                numeroCorsi
        );
    }
}