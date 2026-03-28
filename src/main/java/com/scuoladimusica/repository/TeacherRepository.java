package com.scuoladimusica.repository;

import com.scuoladimusica.model.entity.Teacher;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository per la gestione degli insegnanti.
 *
 *
 * 1. Metodo per trovare un insegnante tramite la sua matricola insegnante.
 *    Deve restituire un Optional<Teacher>.
 *
 * 2. Metodo per verificare se esiste un insegnante con una data matricola insegnante.
 *    Deve restituire un boolean.
 *
 * 3. Metodo per verificare se esiste un insegnante con un dato codice fiscale.
 *    Deve restituire un boolean.
 *
 * SUGGERIMENTO: Il campo si chiama "matricolaInsegnante" nell'entity,
 * quindi il metodo sarà findByMatricolaInsegnante(...)
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional <Teacher> findByMatricolaInsegnante(String matricolaInsegnante);

    Boolean existsByMatricolaInsegnante(String matricolaInsegnante);

    Boolean existsByCf(String teacherCf);

    void deleteByMatricolaInsegnante (String matricolaInsegnante);

}
