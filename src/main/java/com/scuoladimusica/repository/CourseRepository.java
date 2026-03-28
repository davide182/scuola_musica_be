package com.scuoladimusica.repository;

import com.scuoladimusica.model.entity.Course;
import com.scuoladimusica.model.entity.Livello;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository per la gestione dei corsi.
 *
 *
 * 1. Metodo per trovare un corso tramite il suo codice corso.
 *    Deve restituire un Optional<Course>.
 *
 * 2. Metodo per verificare se esiste un corso con un dato codice corso.
 *    Deve restituire un boolean.
 *
 * 3. Metodo per trovare tutti i corsi online (campo online = true).
 *    Deve restituire una List<Course>.
 *    SUGGERIMENTO: findBy + NomeCampo + True
 *
 * 4. Metodo per trovare tutti i corsi filtrati per livello.
 *    Deve restituire una List<Course>.
 *
 * 5. Metodo per trovare tutti i corsi assegnati a un insegnante (tramite teacher_id).
 *    Deve restituire una List<Course>.
 *    SUGGERIMENTO: findByTeacherId(Long teacherId)
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    

    Optional<Course> findByCodiceCorso(String codiceCorso);

    Boolean existsByCodiceCorso(String codiceCorso);

    List<Course> findByLivello (Livello livello);

    List<Course> findByTeacherId (Long id);


    List<Course> findByOnlineTrue();


}
