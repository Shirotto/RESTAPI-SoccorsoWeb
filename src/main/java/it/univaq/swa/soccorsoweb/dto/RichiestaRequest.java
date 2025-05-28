package it.univaq.swa.soccorsoweb.dto;

public class RichiestaRequest {
    private String usersId;
    private String richiedente;
    private String descrizione;
    private String indirizzo;
    private String telefonoContattoRichiesta;
    private String emailContattoRichiesta;

    public RichiestaRequest() {}

    // Getters and Setters
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
}