package it.univaq.swa.soccorsoweb.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "missioni")
public class Missione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "autista_id", nullable = false)
    private Long autistaId;

    @Column(name = "caposquadra_id", nullable = false)
    private Long caposquadraId;

    @Column(nullable = false)
    private String status; // esempio: "in_corso", "chiusa"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getAutistaId() {
        return autistaId;
    }

    public void setAutistaId(Long autistaId) {
        this.autistaId = autistaId;
    }

    public Long getCaposquadraId() {
        return caposquadraId;
    }

    public void setCaposquadraId(Long caposquadraId) {
        this.caposquadraId = caposquadraId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

 
}
