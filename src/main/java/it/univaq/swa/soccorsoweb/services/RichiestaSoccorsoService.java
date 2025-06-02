package it.univaq.swa.soccorsoweb.services;

import it.univaq.swa.soccorsoweb.model.Richiesta_soccorso;
import it.univaq.swa.soccorsoweb.model.StatoRichiesta;
import it.univaq.swa.soccorsoweb.utils.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RichiestaSoccorsoService {
    private static final Logger logger = Logger.getLogger(RichiestaSoccorsoService.class.getName());
    private final EmailService emailService = new EmailService();
    
    /**
     * Salva una nuova richiesta di soccorso
     */
    public Richiesta_soccorso saveRichiesta(Richiesta_soccorso richiesta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            
            // Imposta valori di default
            richiesta.setStatoRichiesta(StatoRichiesta.PENDING_VALIDATION);
            richiesta.setDataCreazione(LocalDateTime.now());
            richiesta.setValidationToken(UUID.randomUUID().toString());
            
            em.persist(richiesta);
            em.getTransaction().commit();
            
            // Invia email di convalida
            sendValidationEmail(richiesta);
            
            return richiesta;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Errore nel salvare la richiesta di soccorso", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Invia email con link di convalida
     */
    private void sendValidationEmail(Richiesta_soccorso richiesta) {
        // Verifica che ci sia un indirizzo email per inviare il link di convalida
        if (richiesta.getEmailContattoRichiesta() != null && !richiesta.getEmailContattoRichiesta().trim().isEmpty()) {
            try {
                boolean sent = emailService.sendValidationEmail(
                    richiesta.getEmailContattoRichiesta(),
                    richiesta.getRichiedente(), 
                    richiesta.getId(), 
                    richiesta.getValidationToken()
                );
                
                if (sent) {
                    logger.info("Email di convalida inviata per la richiesta ID: " + richiesta.getId());
                } else {
                    logger.warning("Invio email di convalida fallito per la richiesta ID: " + richiesta.getId());
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Errore nell'invio dell'email di convalida", e);
            }
        } else {
            logger.warning("Nessun indirizzo email disponibile per l'invio del link di convalida. Richiesta ID: " + richiesta.getId());
        }
    }
    
    /**
     * Invia nuovamente l'email di convalida
     */
    public boolean resendValidationEmail(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Richiesta_soccorso richiesta = em.find(Richiesta_soccorso.class, id);
            if (richiesta == null || richiesta.getStatoRichiesta() != StatoRichiesta.PENDING_VALIDATION) {
                return false;
            }
            
            // Se il token non esiste più, ne creiamo uno nuovo
            if (richiesta.getValidationToken() == null || richiesta.getValidationToken().trim().isEmpty()) {
                em.getTransaction().begin();
                richiesta.setValidationToken(UUID.randomUUID().toString());
                em.merge(richiesta);
                em.getTransaction().commit();
            }
            
            // Invia l'email
            sendValidationEmail(richiesta);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Errore nel reinvio dell'email di convalida", e);
            return false;
        } finally {
            em.close();
        }
    }
    
    // Resto dei metodi rimane invariato...

    /**
     * Trova una richiesta per ID
     */
    public Richiesta_soccorso findRichiestaById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Richiesta_soccorso.class, id);
        } finally {
            em.close();
        }
    }
    
    /**
     * Trova tutte le richieste
     */
    public List<Richiesta_soccorso> findAllRichieste() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Richiesta_soccorso> query = em.createQuery(
                "SELECT r FROM Richiesta_soccorso r ORDER BY r.dataCreazione DESC", 
                Richiesta_soccorso.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Trova richieste per user ID
     */
    public List<Richiesta_soccorso> findRichiesteByUserId(Long userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Richiesta_soccorso> query = em.createQuery(
                "SELECT r FROM Richiesta_soccorso r WHERE r.usersId = :userId ORDER BY r.dataCreazione DESC", 
                Richiesta_soccorso.class
            );
            query.setParameter("userId", userId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Trova richieste per stato
     */
    public List<Richiesta_soccorso> findRichiesteByStato(StatoRichiesta stato) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Richiesta_soccorso> query = em.createQuery(
                "SELECT r FROM Richiesta_soccorso r WHERE r.statoRichiesta = :stato ORDER BY r.dataCreazione DESC", 
                Richiesta_soccorso.class
            );
            query.setParameter("stato", stato);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Trova richiesta per validation token
     */
    public Richiesta_soccorso findRichiestaByValidationToken(String token) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Richiesta_soccorso> query = em.createQuery(
                "SELECT r FROM Richiesta_soccorso r WHERE r.validationToken = :token", 
                Richiesta_soccorso.class
            );
            query.setParameter("token", token);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null; // Richiesta non trovata
        } finally {
            em.close();
        }
    }
    
    /**
     * Aggiorna lo stato di una richiesta (metodo originale per retrocompatibilità)
     */
    public Richiesta_soccorso updateStatoRichiesta(Long id, StatoRichiesta nuovoStato) {
        return updateStatoRichiesta(id, nuovoStato, null);
    }
    
    /**
     * Aggiorna lo stato di una richiesta con livello di successo
     */
    public Richiesta_soccorso updateStatoRichiesta(Long id, StatoRichiesta nuovoStato, String livelloSuccesso) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            
            Richiesta_soccorso richiesta = em.find(Richiesta_soccorso.class, id);
            if (richiesta != null) {
                richiesta.setStatoRichiesta(nuovoStato);
                
                // Se lo stato è CHIUSA e il livello di successo è fornito, lo imposta
                if (nuovoStato == StatoRichiesta.CHIUSA && livelloSuccesso != null && !livelloSuccesso.trim().isEmpty()) {
                    richiesta.setLivelloSuccesso(livelloSuccesso.trim());
                }
                
                em.merge(richiesta);
            }
            
            em.getTransaction().commit();
            return richiesta;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Errore nell'aggiornare lo stato della richiesta", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Aggiorna il livello di successo di una richiesta
     */
    public Richiesta_soccorso updateLivelloSuccesso(Long id, String livelloSuccesso) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            
            Richiesta_soccorso richiesta = em.find(Richiesta_soccorso.class, id);
            if (richiesta != null) {
                richiesta.setLivelloSuccesso(livelloSuccesso);
                em.merge(richiesta);
            }
            
            em.getTransaction().commit();
            return richiesta;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Errore nell'aggiornare il livello di successo", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Aggiorna una richiesta completa
     */
    public Richiesta_soccorso updateRichiesta(Richiesta_soccorso richiesta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            
            Richiesta_soccorso updatedRichiesta = em.merge(richiesta);
            
            em.getTransaction().commit();
            return updatedRichiesta;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Errore nell'aggiornare la richiesta", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Elimina una richiesta
     */
    public boolean deleteRichiesta(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            
            Richiesta_soccorso richiesta = em.find(Richiesta_soccorso.class, id);
            if (richiesta != null) {
                em.remove(richiesta);
                em.getTransaction().commit();
                return true;
            }
            
            em.getTransaction().commit();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Errore nell'eliminare la richiesta", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Conta le richieste per stato
     */
    public Long countRichiesteByStato(StatoRichiesta stato) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(r) FROM Richiesta_soccorso r WHERE r.statoRichiesta = :stato", 
                Long.class
            );
            query.setParameter("stato", stato);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    /**
     * Trova richieste create in un periodo specifico
     */
    public List<Richiesta_soccorso> findRichiesteByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Richiesta_soccorso> query = em.createQuery(
                "SELECT r FROM Richiesta_soccorso r WHERE r.dataCreazione BETWEEN :startDate AND :endDate ORDER BY r.dataCreazione DESC", 
                Richiesta_soccorso.class
            );
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Trova richieste per stato e periodo
     */
    public List<Richiesta_soccorso> findRichiesteByStatoAndPeriod(StatoRichiesta stato, LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Richiesta_soccorso> query = em.createQuery(
                "SELECT r FROM Richiesta_soccorso r WHERE r.statoRichiesta = :stato AND r.dataCreazione BETWEEN :startDate AND :endDate ORDER BY r.dataCreazione DESC", 
                Richiesta_soccorso.class
            );
            query.setParameter("stato", stato);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Conta tutte le richieste
     */
    public Long countAllRichieste() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(r) FROM Richiesta_soccorso r", 
                Long.class
            );
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    /**
     * Conta le richieste per utente
     */
    public Long countRichiesteByUserId(Long userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(r) FROM Richiesta_soccorso r WHERE r.usersId = :userId", 
                Long.class
            );
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    /**
     * Convalida una richiesta (cambia stato da PENDING_VALIDATION ad ATTIVA)
     * Verifica che il token corrisponda a quello della richiesta
     */
    public Richiesta_soccorso convalidaRichiesta(Long id, String validationToken) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            
            Richiesta_soccorso richiesta = em.find(Richiesta_soccorso.class, id);
            if (richiesta == null || richiesta.getStatoRichiesta() != StatoRichiesta.PENDING_VALIDATION) {
                return null;
            }
            
            // Verifica che il token corrisponda a quello della richiesta
            if (validationToken == null || !validationToken.equals(richiesta.getValidationToken())) {
                throw new SecurityException("Token di convalida non valido");
            }
            
            richiesta.setStatoRichiesta(StatoRichiesta.ATTIVA);
            // Rimuovi il token per sicurezza dopo la convalida
            richiesta.setValidationToken(null);
            
            em.merge(richiesta);
            em.getTransaction().commit();
            return richiesta;
        } catch (SecurityException se) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw se;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Errore nella convalida della richiesta", e);
        } finally {
            em.close();
        }
    }
}