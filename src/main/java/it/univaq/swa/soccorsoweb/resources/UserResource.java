package it.univaq.swa.soccorsoweb.resources;

import it.univaq.swa.soccorsoweb.model.User;
import it.univaq.swa.soccorsoweb.services.UserService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserService userService = new UserService();
 
    
    //questo era per vedere se funzionava  poi lo togliamo tranquillamente
    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok("pong from user resource").build();
    }

    @POST
    public Response registerUser(User user) {
        try {
            userService.saveUser(user);
            return Response.status(Response.Status.CREATED).entity(user).build();
        } catch (Exception e) {
            return Response.serverError().entity("Errore nel creare utente: " + e.getMessage()).build();
        }
    }
    
    
    @GET
    public Response getAllUsers(){
  
       List<User> users = (List<User>) userService.findAllUsers();
           return Response.ok(users).build();
        
    }
   

    @GET
    @Path("{id}")
    public Response getUser(@PathParam("id") Long id) {
        User user = userService.findUserById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(user).build();
    }
}