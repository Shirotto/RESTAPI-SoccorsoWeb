package it.univaq.swa.soccorsoweb.config;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        
        packages("it.univaq.swa.soccorsoweb.resources");
        
    }
}