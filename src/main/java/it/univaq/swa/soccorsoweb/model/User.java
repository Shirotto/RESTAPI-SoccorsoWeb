package it.univaq.swa.soccorsoweb.model;

import jakarta.persistence.*;

@Entity
@Table(name="users")
public class User {
    
   @Id 
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   
    @Column(name = "nome", nullable = false)
    private String nome;
    
    @Column(name = "cognome", nullable = false)
    private String cognome;
    
    @Column(name = "telefono", nullable = false)
    private String telefono; 
    
    @Column(name = "indirizzo")
    private String indirizzo;
    
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.UTENTE;
    
    public User() {}
   
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    // Metodi helper per controllare i ruoli
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }
    
    public boolean isUtente() {
        return UserRole.UTENTE.equals(this.role);
    }
}