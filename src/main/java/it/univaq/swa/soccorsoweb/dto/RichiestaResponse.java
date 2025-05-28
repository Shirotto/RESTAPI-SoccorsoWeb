package it.univaq.swa.soccorsoweb.dto;

import it.univaq.swa.soccorsoweb.model.Richiesta_soccorso;

public class RichiestaResponse {
    private String message;
    private Richiesta_soccorso richiesta;

    public RichiestaResponse() {}

    public RichiestaResponse(String message) {
        this.message = message;
    }

    public RichiestaResponse(String message, Richiesta_soccorso richiesta) {
        this.message = message;
        this.richiesta = richiesta;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Richiesta_soccorso getRichiesta() {
        return richiesta;
    }

    public void setRichiesta(Richiesta_soccorso richiesta) {
        this.richiesta = richiesta;
    }
}