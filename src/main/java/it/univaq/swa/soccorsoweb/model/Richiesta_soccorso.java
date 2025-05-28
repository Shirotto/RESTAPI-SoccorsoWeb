package it.univaq.swa.soccorsoweb.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="richieste_soccorso")
public class Richiesta_soccorso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "users_id")
    private String usersId;

    @Column(name = "richiedente") 
    private String richiedente;

    @Column(name = "descrizione", nullable = false) 
    private String descrizione;

    @Column(name = "indirizzo", nullable = false) 
    private String indirizzo;

    @Column(name = "telefono_contatto_richiesta") 
    private String telefonoContattoRichiesta;

    @Column(name = "email_contatto_richiesta") 
    private String emailContattoRichiesta;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato_richiesta", nullable = false)
    private StatoRichiesta statoRichiesta;

    @Column(name = "livello_successo")
    private String livelloSuccesso;

    @Column(name = "data_creazione", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime dataCreazione;

    @Column(name = "validation_token", unique = true) 
    private String validationToken;

    public Richiesta_soccorso() {}

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsersId() {
        return usersId;
    }

    public void setUsersId(String usersId) {
        this.usersId = usersId;
    }

    public String getRichiedente() {
        return richiedente;
    }

    public void setRichiedente(String richiedente) {
        this.richiedente = richiedente;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getTelefonoContattoRichiesta() {
        return telefonoContattoRichiesta;
    }

    public void setTelefonoContattoRichiesta(String telefonoContattoRichiesta) {
        this.telefonoContattoRichiesta = telefonoContattoRichiesta;
    }

    public String getEmailContattoRichiesta() {
        return emailContattoRichiesta;
    }

    public void setEmailContattoRichiesta(String emailContattoRichiesta) {
        this.emailContattoRichiesta = emailContattoRichiesta;
    }

    public StatoRichiesta getStatoRichiesta() {
        return statoRichiesta;
    }

    public void setStatoRichiesta(StatoRichiesta statoRichiesta) {
        this.statoRichiesta = statoRichiesta;
    }

    public String getLivelloSuccesso() {
        return livelloSuccesso;
    }

    public void setLivelloSuccesso(String livelloSuccesso) {
        this.livelloSuccesso = livelloSuccesso;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public String getValidationToken() {
        return validationToken;
    }

    public void setValidationToken(String validationToken) {
        this.validationToken = validationToken;
    }
}