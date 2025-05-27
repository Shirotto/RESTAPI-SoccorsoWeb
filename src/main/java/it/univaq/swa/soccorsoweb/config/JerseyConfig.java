package it.univaq.swa.soccorsoweb.config;

import it.univaq.swa.soccorsoweb.resources.UserResource;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
       
       
        register(UserResource.class);
    }
}
