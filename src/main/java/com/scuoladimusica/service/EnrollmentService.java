package com.scuoladimusica.service;

import com.scuoladimusica.exception.BusinessRuleException;
import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.response.EnrollmentResponse;
import com.scuoladimusica.model.entity.Course;
import com.scuoladimusica.model.entity.Enrollment;
import com.scuoladimusica.model.entity.Student;
import com.scuoladimusica.repository.CourseRepository;
import com.scuoladimusica.repository.EnrollmentRepository;
import com.scuoladimusica.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * Iscrivere uno studente a un corso.
     */
    public EnrollmentResponse enrollStudent(String matricola, String codiceCorso, int annoIscrizione) {
        Student student = studentRepository.findByMatricola(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Studente non trovato con matricola: " + matricola));
        
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));
        
        // Verificare che lo studente non sia già iscritto allo stesso corso
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new DuplicateResourceException("Studente già iscritto a questo corso");
        }
        
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .annoIscrizione(annoIscrizione)
                .build();
        
        Enrollment saved = enrollmentRepository.save(enrollment);
        
        return new EnrollmentResponse(
                saved.getId(),
                saved.getStudent().getMatricola(),
                saved.getStudent().getNomeCompleto(),
                saved.getCourse().getCodiceCorso(),
                saved.getCourse().getNome(),
                saved.getAnnoIscrizione(),
                saved.getVotoFinale()
        );
    }

    /**
     * Registrare un voto per un'iscrizione.
     */
    public EnrollmentResponse registerVote(String matricola, String codiceCorso, int voto) {
        // Verificare che il voto sia tra 18 e 30
        if (voto < 18 || voto > 30) {
                throw new BusinessRuleException("Il voto deve essere compreso tra 18 e 30");
        }
        
        Enrollment enrollment = enrollmentRepository.findByStudentMatricolaAndCourseCodiceCorso(matricola, codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Iscrizione non trovata per studente " + matricola + " e corso " + codiceCorso));
        
        enrollment.setVotoFinale(voto);
        Enrollment saved = enrollmentRepository.save(enrollment);
        
        return new EnrollmentResponse(
                saved.getId(),
                saved.getStudent().getMatricola(),
                saved.getStudent().getNomeCompleto(),
                saved.getCourse().getCodiceCorso(),
                saved.getCourse().getNome(),
                saved.getAnnoIscrizione(),
                saved.getVotoFinale()
                );
        }

    /**
     * Recuperare tutte le iscrizioni di uno studente.
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudent(String matricola) {
        Student student = studentRepository.findByMatricola(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Studente non trovato con matricola: " + matricola));
        
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        List<EnrollmentResponse> responses = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            responses.add(new EnrollmentResponse(
                    enrollment.getId(),
                    enrollment.getStudent().getMatricola(),
                    enrollment.getStudent().getNomeCompleto(),
                    enrollment.getCourse().getCodiceCorso(),
                    enrollment.getCourse().getNome(),
                    enrollment.getAnnoIscrizione(),
                    enrollment.getVotoFinale()
            ));
        }
        
        return responses;
    }

    /**
     * Recuperare tutte le iscrizioni per un corso.
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByCourse(String codiceCorso) {
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));
        
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());
        List<EnrollmentResponse> responses = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            responses.add(new EnrollmentResponse(
                    enrollment.getId(),
                    enrollment.getStudent().getMatricola(),
                    enrollment.getStudent().getNomeCompleto(),
                    enrollment.getCourse().getCodiceCorso(),
                    enrollment.getCourse().getNome(),
                    enrollment.getAnnoIscrizione(),
                    enrollment.getVotoFinale()
            ));
        }
        
        return responses;
    }
}