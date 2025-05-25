package it.univaq.swa.soccorsoweb.model;

import java.time.LocalDateTime;

public class RichiestaSoccorso { 
    private Long id;
    private String descrizione;
    private String indirizzo;
    private String telefono;
    private String email;
    private StatoRichiesta stato;
    private Integer livelloSuccesso;
    private LocalDateTime dataCreazione;
}

enum StatoRichiesta {
    ATTIVA, IN_CORSO, CHIUSA, IGNORATA
}