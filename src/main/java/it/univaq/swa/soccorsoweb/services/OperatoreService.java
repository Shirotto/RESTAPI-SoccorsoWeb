package it.univaq.swa.soccorsoweb.services;

import it.univaq.swa.soccorsoweb.model.Operatore;
import it.univaq.swa.soccorsoweb.utils.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class OperatoreService {

    public Operatore saveOperatore(Operatore operatore) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(operatore);
            em.getTransaction().commit();
            return operatore;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Operatore findOperatoreById(Integer id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Operatore.class, id);
        } finally {
            em.close();
        }
    }

    public List<Operatore> findAllOperatori() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Operatore> query = em.createQuery("SELECT o FROM Operatore o", Operatore.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
      // Nuovo metodo per filtro disponibile
    public List<Operatore> findOperatoriByDisponibile(boolean disponibile) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Operatore> query = em.createQuery(
                "SELECT o FROM Operatore o WHERE o.disponibile = :disp", Operatore.class);
            query.setParameter("disp", disponibile);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
