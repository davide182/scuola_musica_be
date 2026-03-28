package com.scuoladimusica.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "instruments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Il codice strumento non può essere vuoto")
    @Column(unique = true, nullable = false)
    private String codiceStrumento;

    @NotBlank(message = "Il nome dello strumento non può essere vuoto")
    @Column(nullable = false)
    private String nome;

    @NotNull(message = "Il tipo strumento è obbligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoStrumento tipoStrumento;

    private String marca;

    @Max(value = 2026, message = "L'anno di produzione non può essere nel futuro")
    private Integer annoProduzione;

    // Campi specifici per strumenti a corda
    private Integer numeroCorde;
    private String tipoCorde;
    private String materiale;

    // Campi specifici per percussioni
    private String tipoPelle;
    private Double diametro;

    @OneToMany(mappedBy = "instrument", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Loan> loans = new ArrayList<>();

    /**
     * Verifica se lo strumento è disponibile (nessun prestito attivo).
     * Un prestito è attivo quando dataFine è null.
     */
    public boolean isDisponibile() {
        return loans.stream().noneMatch(l -> l.getDataFine() == null);
    }
}
