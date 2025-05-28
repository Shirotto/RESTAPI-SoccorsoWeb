package it.univaq.swa.soccorsoweb.services;

import it.univaq.swa.soccorsoweb.model.User;
import it.univaq.swa.soccorsoweb.utils.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class UserService {
    
    /**
     * Salva un nuovo utente nel database
     */
    public void saveUser(User user) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();           
            // Hash della password prima di salvare
            user.setPassword(hashPassword(user.getPassword()));            
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Errore nel salvare l'utente", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Trova un utente per ID
     */
    public User findUserById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }
    
    /**
     * Trova tutti gli utenti
     */
    public List<User> findAllUsers() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Trova un utente per email
     */
    public User findUserByEmail(String email) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null; // Utente non trovato
        } finally {
            em.close();
        }
    }
    
    /**
     * Autentica un utente con email e password
     */
    public User authenticateUser(String email, String password) {
        User user = findUserByEmail(email);
        if (user != null && verifyPassword(password, user.getPassword())) {
            return user;
        }
        return null;
    }
    
    /**
     * Hash della password usando SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());           
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();           
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Errore nell'hashing della password", e);
        }
    }
    
    /**
     * Verifica se la password inserita corrisponde a quella hashata
     */
    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        return hashPassword(rawPassword).equals(hashedPassword);
    }
}