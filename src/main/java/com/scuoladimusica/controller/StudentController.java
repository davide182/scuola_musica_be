package com.scuoladimusica.controller;

import com.scuoladimusica.model.dto.request.StudentRequest;
import com.scuoladimusica.model.dto.response.StudentReportResponse;
import com.scuoladimusica.model.dto.response.StudentResponse;
import com.scuoladimusica.model.entity.Livello;
import com.scuoladimusica.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        List<StudentResponse> responses = studentService.getAllStudents();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{matricola}")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable String matricola) {
        StudentResponse response = studentService.getStudentByMatricola(matricola);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/livello/{livello}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<StudentResponse>> getStudentsByLivello(@PathVariable Livello livello) {
        List<StudentResponse> responses = studentService.getStudentsByLivello(livello);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{matricola}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable String matricola,
            @Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.updateStudent(matricola, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{matricola}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable String matricola) {
        studentService.deleteStudent(matricola);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{matricola}/report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<StudentReportResponse> getStudentReport(@PathVariable String matricola) {
        StudentReportResponse response = studentService.getStudentReport(matricola);
        return ResponseEntity.ok(response);
    }
}