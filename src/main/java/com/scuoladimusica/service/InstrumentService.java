package com.scuoladimusica.service;

import com.scuoladimusica.exception.BusinessRuleException;
import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.request.InstrumentRequest;
import com.scuoladimusica.model.dto.response.InstrumentResponse;
import com.scuoladimusica.model.dto.response.LoanResponse;
import com.scuoladimusica.model.entity.Instrument;
import com.scuoladimusica.model.entity.Loan;
import com.scuoladimusica.model.entity.Student;
import com.scuoladimusica.repository.InstrumentRepository;
import com.scuoladimusica.repository.LoanRepository;
import com.scuoladimusica.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InstrumentService {

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private StudentRepository studentRepository;

    public InstrumentResponse createInstrument(InstrumentRequest request) {
        if (instrumentRepository.existsByCodiceStrumento(request.codiceStrumento())) {
            throw new DuplicateResourceException("Strumento già esistente con codice strumento : " + request.codiceStrumento());
        }
        
        Instrument instrument = Instrument.builder()
                .codiceStrumento(request.codiceStrumento())
                .nome(request.nome())
                .tipoStrumento(request.tipoStrumento())
                .marca(request.marca())
                .annoProduzione(request.annoProduzione())
                .numeroCorde(request.numeroCorde())
                .tipoCorde(request.tipoCorde())
                .materiale(request.materiale())
                .tipoPelle(request.tipoPelle())
                .diametro(request.diametro())
                .build();

        Instrument instrumentSave = instrumentRepository.save(instrument);
        
        return new InstrumentResponse(
                instrumentSave.getId(),
                instrumentSave.getCodiceStrumento(),
                instrumentSave.getNome(),
                instrumentSave.getTipoStrumento(),
                instrumentSave.getMarca(),
                instrumentSave.getAnnoProduzione(),
                true  // nuovo strumento è sempre disponibile
        );
    }

    @Transactional(readOnly = true)
    public InstrumentResponse getInstrumentByCode(String codiceStrumento) {
        Instrument instrument = instrumentRepository.findByCodiceStrumento(codiceStrumento)
                .orElseThrow(() -> new ResourceNotFoundException("Strumento non trovato con codice strumento : " + codiceStrumento));
        
        // Verifica disponibilità tramite repository
        boolean disponibile = !loanRepository.existsByInstrumentIdAndDataFineIsNull(instrument.getId());
        
        return new InstrumentResponse(
                instrument.getId(),
                instrument.getCodiceStrumento(),
                instrument.getNome(),
                instrument.getTipoStrumento(),
                instrument.getMarca(),
                instrument.getAnnoProduzione(),
                disponibile
        );
    }

    @Transactional(readOnly = true)
    public List<InstrumentResponse> getAllInstruments() {
        List<Instrument> instruments = instrumentRepository.findAll();
        List<InstrumentResponse> instrumentSalvati = new ArrayList<>();

        for (Instrument instrument : instruments) {
            boolean disponibile = !loanRepository.existsByInstrumentIdAndDataFineIsNull(instrument.getId());
            instrumentSalvati.add(new InstrumentResponse(
                    instrument.getId(),
                    instrument.getCodiceStrumento(),
                    instrument.getNome(),
                    instrument.getTipoStrumento(),
                    instrument.getMarca(),
                    instrument.getAnnoProduzione(),
                    disponibile
            ));
        }
        return instrumentSalvati;
    }

    @Transactional(readOnly = true)
    public List<InstrumentResponse> getAvailableInstruments() {
        List<Instrument> allInstruments = instrumentRepository.findAll();
        List<InstrumentResponse> instrumentsDisponibili = new ArrayList<>();

        for (Instrument instrument : allInstruments) {
            boolean disponibile = !loanRepository.existsByInstrumentIdAndDataFineIsNull(instrument.getId());
            if (disponibile) {
                instrumentsDisponibili.add(new InstrumentResponse(
                        instrument.getId(),
                        instrument.getCodiceStrumento(),
                        instrument.getNome(),
                        instrument.getTipoStrumento(),
                        instrument.getMarca(),
                        instrument.getAnnoProduzione(),
                        true
                ));
            }
        }
        return instrumentsDisponibili;
    }

    public LoanResponse loanToStudent(String codiceStrumento, String matricolaStudente, LocalDate dataInizio) {
        Instrument instrument = instrumentRepository.findByCodiceStrumento(codiceStrumento)
                .orElseThrow(() -> new ResourceNotFoundException("Strumento non trovato per codice strumento: " + codiceStrumento));
        
        Student student = studentRepository.findByMatricola(matricolaStudente)
                .orElseThrow(() -> new ResourceNotFoundException("Studente non trovato con matricola : " + matricolaStudente));
        
        // Verifica se esiste già un prestito attivo
        boolean hasActiveLoan = loanRepository.existsByInstrumentIdAndDataFineIsNull(instrument.getId());
        if (hasActiveLoan) {
            throw new BusinessRuleException("Strumento non disponibile");
        }
        
        Loan loan = Loan.builder()
                .instrument(instrument)
                .student(student)
                .dataInizio(dataInizio)
                .dataFine(null)
                .build();

        Loan savedLoan = loanRepository.save(loan);

        return new LoanResponse(
                savedLoan.getId(),
                savedLoan.getInstrument().getCodiceStrumento(),
                savedLoan.getInstrument().getNome(),
                savedLoan.getStudent().getMatricola(),
                savedLoan.getStudent().getNomeCompleto(),
                savedLoan.getDataInizio(),
                savedLoan.getDataFine()
        );
    }

    public LoanResponse returnInstrument(String codiceStrumento, LocalDate dataFine) {
        Instrument instrument = instrumentRepository.findByCodiceStrumento(codiceStrumento)
                .orElseThrow(() -> new ResourceNotFoundException("Strumento non trovato con codice strumento : " + codiceStrumento));
        
        // Trova il prestito attivo
        Optional<Loan> activeLoanOpt = loanRepository.findByInstrumentIdAndDataFineIsNull(instrument.getId());
        
        if (activeLoanOpt.isEmpty()) {
            throw new BusinessRuleException("Nessun prestito attivo trovato per lo strumento: " + codiceStrumento);
        }
        
        Loan activeLoan = activeLoanOpt.get();
        
        if (dataFine.isBefore(activeLoan.getDataInizio())) {
            throw new BusinessRuleException("La data di restituzione non può essere precedente alla data di inizio prestito");
        }
        
        activeLoan.setDataFine(dataFine);
        Loan savedLoan = loanRepository.save(activeLoan);

        return new LoanResponse(
                savedLoan.getId(),
                savedLoan.getInstrument().getCodiceStrumento(),
                savedLoan.getInstrument().getNome(),
                savedLoan.getStudent().getMatricola(),
                savedLoan.getStudent().getNomeCompleto(),
                savedLoan.getDataInizio(),
                savedLoan.getDataFine()
        );
    }
}