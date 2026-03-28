package com.scuoladimusica.controller;

import com.scuoladimusica.model.dto.request.CourseRequest;
import com.scuoladimusica.model.dto.request.LessonRequest;
import com.scuoladimusica.model.dto.response.CourseResponse;
import com.scuoladimusica.model.dto.response.LessonResponse;
import com.scuoladimusica.model.dto.response.MessageResponse;
import com.scuoladimusica.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        List<CourseResponse> responses = courseService.getAllCourses();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/online")
    public ResponseEntity<List<CourseResponse>> getOnlineCourses() {
        List<CourseResponse> responses = courseService.getOnlineCourses();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{codiceCorso}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable String codiceCorso) {
        CourseResponse response = courseService.getCourseByCode(codiceCorso);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{codiceCorso}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable String codiceCorso,
            @Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.updateCourse(codiceCorso, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{codiceCorso}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable String codiceCorso) {
        courseService.deleteCourse(codiceCorso);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{codiceCorso}/lessons")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<LessonResponse> addLesson(
            @PathVariable String codiceCorso,
            @Valid @RequestBody LessonRequest request) {
        LessonResponse response = courseService.addLesson(codiceCorso, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{codiceCorso}/instruments/{codiceStrumento}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> addInstrument(
            @PathVariable String codiceCorso,
            @PathVariable String codiceStrumento) {
        courseService.addInstrumentToCourse(codiceCorso, codiceStrumento);
        return ResponseEntity.ok(new MessageResponse("Strumento aggiunto al corso"));
    }
}