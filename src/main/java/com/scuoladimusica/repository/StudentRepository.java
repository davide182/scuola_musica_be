package com.scuoladimusica.repository;

import com.scuoladimusica.model.entity.Livello;
import com.scuoladimusica.model.entity.Student;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository per la gestione degli studenti.
 *
 * Estendendo JpaRepository si ottengono automaticamente i metodi CRUD di base:
 * save(), findById(), findAll(), deleteById(), count(), ecc.
 *
 * 
 * 
 *
 * 1. Metodo per trovare uno studente tramite la sua matricola.
 *    Deve restituire un Optional<Student>.
 *
 * 2. Metodo per verificare se esiste uno studente con una data matricola.
 *    Deve restituire un boolean.
 *
 * 3. Metodo per verificare se esiste uno studente con un dato codice fiscale.
 *    Deve restituire un boolean.
 *
 * 4. Metodo per trovare tutti gli studenti che hanno un certo livello.
 *    Deve restituire una List<Student>.
 *    Il parametro è di tipo Livello (enum).
 *
 * SUGGERIMENTO: Spring Data JPA genera automaticamente l'implementazione
 * a partire dal nome del metodo. Ad esempio:
 *   Optional<Entity> findByNomeCampo(TipoCampo valore)
 *   boolean existsByNomeCampo(TipoCampo valore)
 *   List<Entity> findByNomeCampo(TipoCampo valore)
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional <Student> findByMatricola(String matricola);

    Boolean existsByMatricola(String matricola);

    Boolean existsByCf(String cf);

    List<Student> findByLivello(Livello livello);

    void deleteByMatricola(String matricola);

}
