package com.scuoladimusica.service;

import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.request.StudentRequest;
import com.scuoladimusica.model.dto.response.StudentReportResponse;
import com.scuoladimusica.model.dto.response.StudentResponse;
import com.scuoladimusica.model.entity.Enrollment;
import com.scuoladimusica.model.entity.Livello;
import com.scuoladimusica.model.entity.Student;
import com.scuoladimusica.repository.EnrollmentRepository;
import com.scuoladimusica.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.existsByMatricola(request.matricola())) {
            throw new DuplicateResourceException("Matricola già in uso!");
        }
        if (studentRepository.existsByCf(request.cf())) {
            throw new DuplicateResourceException("Codice fiscale già in uso!");
        }
        
        Livello livello = request.livello() != null ? request.livello() : Livello.PRINCIPIANTE;
        
        Student student = Student.builder()
                .matricola(request.matricola())
                .cf(request.cf())
                .nome(request.nome())
                .cognome(request.cognome())
                .dataNascita(request.dataNascita())
                .telefono(request.telefono())
                .livello(livello)
                .build();
        
        Student saved = studentRepository.save(student);
        
        return mapToStudentResponse(saved);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentByMatricola(String matricola) {
        Student student = studentRepository.findByMatricola(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Matricola non trovata : " + matricola));
        
        return mapToStudentResponse(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        List<StudentResponse> responses = new ArrayList<>();
        
        // CORRETTO: restituisci lista vuota invece di lanciare eccezione
        for (Student student : students) {
            responses.add(mapToStudentResponse(student));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByLivello(Livello livello) {
        List<Student> students = studentRepository.findByLivello(livello);
        List<StudentResponse> response = new ArrayList<>();
        
        // CORRETTO: restituisci lista vuota invece di lanciare eccezione
        for (Student student : students) {
            response.add(mapToStudentResponse(student));
        }
        return response;
    }

    public StudentResponse updateStudent(String matricola, StudentRequest request) {
        Student student = studentRepository.findByMatricola(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con matricola : " + matricola));
        
        student.setNome(request.nome());
        student.setCognome(request.cognome());
        student.setTelefono(request.telefono());

        if (request.livello() != null) {
            student.setLivello(request.livello());
        }
        
        Student saved = studentRepository.save(student);
        
        return mapToStudentResponse(saved);
    }

    public void deleteStudent(String matricola) {
        Student student = studentRepository.findByMatricola(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con matricola: " + matricola));
        studentRepository.delete(student);
    }

    @Transactional(readOnly = true)
    public StudentReportResponse getStudentReport(String matricola) {
        Student student = studentRepository.findByMatricola(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con matricola: " + matricola));
        
        // CARICA LE ISCRIZIONI DAL DATABASE
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        
        List<String> nomiCorsi = enrollments.stream()
                .map(enrollment -> enrollment.getCourse().getNome())
                .toList();
        
        // CALCOLA MEDIA VOTI
        double mediaVoti = 0.0;
        int countConVoto = 0;
        int sommaVoti = 0;
        
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getVotoFinale() != null) {
                sommaVoti += enrollment.getVotoFinale();
                countConVoto++;
            }
        }
        
        if (countConVoto > 0) {
            mediaVoti = (double) sommaVoti / countConVoto;
        }
        
        return new StudentReportResponse(
                student.getNomeCompleto(),
                student.getLivello(),
                enrollments.size(),  // numero corsi frequentati
                mediaVoti,
                nomiCorsi
        );
    }
    
    private StudentResponse mapToStudentResponse(Student student) {
        // CARICA LE ISCRIZIONI PER CALCOLARE I DATI CORRETTI
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        
        int numeroCorsi = enrollments.size();
        
        double mediaVoti = 0.0;
        int countConVoto = 0;
        int sommaVoti = 0;
        
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getVotoFinale() != null) {
                sommaVoti += enrollment.getVotoFinale();
                countConVoto++;
            }
        }
        
        if (countConVoto > 0) {
            mediaVoti = (double) sommaVoti / countConVoto;
        }
        
        return new StudentResponse(
                student.getId(),
                student.getMatricola(),
                student.getCf(),
                student.getNome(),
                student.getCognome(),
                student.getNomeCompleto(),
                student.getDataNascita(),
                student.getTelefono(),
                student.getLivello(),
                numeroCorsi,
                mediaVoti
        );
    }
}