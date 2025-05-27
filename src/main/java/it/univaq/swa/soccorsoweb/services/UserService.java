package it.univaq.swa.soccorsoweb.services;

import it.univaq.swa.soccorsoweb.model.User;
import it.univaq.swa.soccorsoweb.utils.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class UserService {

    public void saveUser(User user) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(user);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
    
    
    public List <User> findAllUsers(){
        
        EntityManager em = JPAUtil.getEntityManager();
        
        try{
            return em.createQuery("Select u FROM User u", User.class).getResultList();
        }finally{
            em.close();
        }
    }
    
    

    public User findUserById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }
}
