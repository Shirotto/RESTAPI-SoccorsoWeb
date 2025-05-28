package it.univaq.swa.soccorsoweb.dto;

import it.univaq.swa.soccorsoweb.model.User;

public class LoginResponse {
    
    private String message;
    private String token;
    private User user;
    private boolean success;
    
    // Costruttore per errori
    public LoginResponse(String message) {
        this.message = message;
        this.success = false;
    }
    
    // Costruttore per successo
    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = user;
        this.message = "Login effettuato con successo";
        this.success = true;
    }
    
    // Costruttore completo
    public LoginResponse(String message, String token, User user, boolean success) {
        this.message = message;
        this.token = token;
        this.user = user;
        this.success = success;
    }
    
    // Getters e Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}