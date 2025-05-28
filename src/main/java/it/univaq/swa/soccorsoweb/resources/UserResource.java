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

    @POST
    public Response registerUser(User user) {
        try {
            // Validazioni input
            if (user == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Dati utente mancanti\"}")
                    .build();
            }
            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Email è obbligatoria\"}")
                    .build();
            }
            
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Password è obbligatoria\"}")
                    .build();
            }
            
            if (user.getNome() == null || user.getNome().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Nome è obbligatorio\"}")
                    .build();
            }
            
            if (user.getCognome() == null || user.getCognome().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Cognome è obbligatorio\"}")
                    .build();
            }
            
            // Validazione telefono (deve essere > 0)
            if (user.getTelefono() <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Numero di telefono non valido\"}")
                    .build();
            }
            
            // Controlla se l'email è già registrata
            User existingUser = userService.findUserByEmail(user.getEmail());
            if (existingUser != null) {
                return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Email già registrata\"}")
                    .build();
            }
            
            // Salva l'utente
            userService.saveUser(user);
            
            // Rimuovi la password dalla risposta per sicurezza
            user.setPassword(null);
            
            return Response.status(Response.Status.CREATED).entity(user).build();
            
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"Il numero di telefono deve contenere solo cifre\"}")
                .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Errore nel creare utente: " + e.getMessage() + "\"}")
                .build();
        }
    }
    
    @GET
    public Response getAllUsers() {
        try {
            List<User> users = userService.findAllUsers();
            
            // Rimuovi le password
            users.forEach(user -> user.setPassword(null));
            
            return Response.ok(users).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Errore nel recuperare gli utenti\"}")
                .build();
        }
    }

    @GET
    @Path("{id}")
    public Response getUser(@PathParam("id") Long id) {
        try {
            User user = userService.findUserById(id);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Utente non trovato\"}")
                    .build();
            }
            
            // Rimuovi la password 
            user.setPassword(null);
            
            return Response.ok(user).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Errore nel recuperare l'utente\"}")
                .build();
        }
    }
}