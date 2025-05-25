package it.univaq.swa.soccorsoweb.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import it.univaq.swa.soccorsoweb.resources.TestResource;
import java.util.HashSet;
import java.util.Set;

/**
 * Configurazione principale per JAX-RS
 * Definisce il path base per tutte le API: /api
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        
        // Registrazione esplicita del TestResource
        classes.add(TestResource.class);
        
        return classes;
    }
}