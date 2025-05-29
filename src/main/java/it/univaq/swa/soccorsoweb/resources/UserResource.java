package it.univaq.swa.soccorsoweb.resources;

import it.univaq.swa.soccorsoweb.model.User;
import it.univaq.swa.soccorsoweb.model.UserRole;
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
            
            // I nuovi utenti sono sempre UTENTE di default (non possono auto-registrarsi come admin)
            user.setRole(UserRole.UTENTE);
            
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
    public Response getAllUsers(@HeaderParam("User-ID") Long currentUserId) {
        try {
            // Solo gli admin possono vedere tutti gli utenti
            User currentUser = userService.findUserById(currentUserId);
            if (currentUser == null || !currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Accesso negato. Solo gli admin possono visualizzare tutti gli utenti\"}")
                    .build();
            }
            
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
    public Response getUser(@PathParam("id") Long id, @HeaderParam("User-ID") Long currentUserId) {
        try {
            User currentUser = userService.findUserById(currentUserId);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Utente non autenticato\"}")
                    .build();
            }
            
            // Un utente normale può vedere solo i propri dati, un admin può vedere tutti
            if (!currentUser.isAdmin() && !currentUser.getId().equals(id)) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Accesso negato. Puoi visualizzare solo i tuoi dati\"}")
                    .build();
            }
            
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
    
    @PUT
    @Path("{id}/role")
    public Response updateUserRole(@PathParam("id") Long userId, 
                                   UserRoleRequest roleRequest,
                                   @HeaderParam("User-ID") Long currentUserId) {
        try {
            User currentUser = userService.findUserById(currentUserId);
            if (currentUser == null || !currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Solo gli admin possono modificare i ruoli\"}")
                    .build();
            }
            
            userService.updateUserRole(userId, roleRequest.getRole(), currentUser);
            
            return Response.ok().entity("{\"message\":\"Ruolo aggiornato con successo\"}").build();
            
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\":\"" + e.getMessage() + "\"}")
                .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Errore nell'aggiornare il ruolo\"}")
                .build();
        }
    }
    
    // Classe per ricevere le richieste di aggiornamento ruolo
    public static class UserRoleRequest {
        private UserRole role;
        
        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
    }
}