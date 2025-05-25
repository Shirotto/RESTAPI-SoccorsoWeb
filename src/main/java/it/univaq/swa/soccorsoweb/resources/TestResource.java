package it.univaq.swa.soccorsoweb.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Resource di test per verificare che JAX-RS funzioni
 */
@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
public class TestResource {
    
    @GET
    public Response test() {
        return Response.ok()
                .entity("{\"message\": \"SoccorsoWeb Services API is running!\", \"status\": \"OK\"}")
                .build();
    }
    
    @GET
    @Path("/info")
    public Response info() {
        return Response.ok()
                .entity("{\"project\": \"SoccorsoWeb Services\", \"version\": \"1.0\", \"author\": \"Shirotto\"}")
                .build();
    }
}