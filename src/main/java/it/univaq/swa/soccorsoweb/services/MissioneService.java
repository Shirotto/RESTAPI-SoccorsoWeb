package it.univaq.swa.soccorsoweb.services;

import it.univaq.swa.soccorsoweb.model.Missione;
import it.univaq.swa.soccorsoweb.model.Operatore;
import it.univaq.swa.soccorsoweb.utils.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

public class MissioneService {

    public void saveMissione(Missione missione) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            missione.setCreatedAt(LocalDateTime.now());
            missione.setStatus("open");
            em.persist(missione);
            
             // Aggiorna autista
            Operatore autista = em.find(Operatore.class, missione.getAutistaId());
            if (autista != null) {
                autista.setDisponibile(false);
                em.merge(autista);
            }

            // Aggiorna caposquadra
            Operatore caposquadra = em.find(Operatore.class, missione.getCaposquadraId());
            if (caposquadra != null) {
                caposquadra.setDisponibile(false);
                em.merge(caposquadra);
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Errore nel salvataggio della missione", e);
        } finally {
            em.close();
        }
    }

    public Missione findMissioneById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Missione.class, id);
        } finally {
            em.close();
        }
    }

    public List<Missione> findMissioniByRequestId(Long requestId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Missione> query = em.createQuery(
                "SELECT m FROM Missione m WHERE m.requestId = :requestId", Missione.class);
            query.setParameter("requestId", requestId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Missione> findMissioniByOperatorId(Long operatorId) {
    EntityManager em = JPAUtil.getEntityManager();
    try {
        TypedQuery<Missione> query = em.createQuery(
            "SELECT m FROM Missione m WHERE m.autistaId = :operatorId OR m.caposquadraId = :operatorId", Missione.class);
        query.setParameter("operatorId", operatorId);
        return query.getResultList();
    } finally {
        em.close();
    }
}


    public void updateMissione(Missione missione) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(missione);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Errore nell'aggiornamento missione", e);
        } finally {
            em.close();
        }
    }
    
    public List<Missione> findMissioniByStatus(String status) {
    EntityManager em = JPAUtil.getEntityManager();
    try {
        return em.createQuery(
            "SELECT m FROM Missione m WHERE m.status = :st", Missione.class)
                 .setParameter("st", status)
                 .getResultList();
    } finally { em.close(); }
}

public Missione closeMissione(Long id) {
    EntityManager em = JPAUtil.getEntityManager();
    try {
        em.getTransaction().begin();
        Missione m = em.find(Missione.class, id);
        if (m == null) { em.getTransaction().rollback(); return null; }

        m.setStatus("closed");
        m.setClosedAt(LocalDateTime.now());

        // libera gli operatori
        Operatore a = em.find(Operatore.class, m.getAutistaId().intValue());
        Operatore c = em.find(Operatore.class, m.getCaposquadraId().intValue());
        if (a != null) a.setDisponibile(true);
        if (c != null) c.setDisponibile(true);

        em.merge(m);     // e gli operatori grazie al dirty-checking
        em.getTransaction().commit();
        return m;
    } catch (Exception e) {
        if (em.getTransaction().isActive()) em.getTransaction().rollback();
        throw e;
    } finally { em.close(); }
}


public List<Missione> findAllMissioni() {
    EntityManager em = JPAUtil.getEntityManager();
    try {
        return em.createQuery("SELECT m FROM Missione m", Missione.class).getResultList();
    } finally {
        em.close();
    }
}

}
