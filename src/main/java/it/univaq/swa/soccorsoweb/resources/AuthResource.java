package it.univaq.swa.soccorsoweb.resources;

import it.univaq.swa.soccorsoweb.dto.LoginRequest;
import it.univaq.swa.soccorsoweb.dto.LoginResponse;
import it.univaq.swa.soccorsoweb.model.User;
import it.univaq.swa.soccorsoweb.security.JwtUtil;
import it.univaq.swa.soccorsoweb.services.UserService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    
    private final JwtUtil jwtUtil = new JwtUtil();
    private final UserService userService = new UserService();
    
    /**
     * Login
     */
    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        try {
            // Validazione input
            if (loginRequest == null || 
                loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty() ||
                loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new LoginResponse("Email e password sono obbligatori"))
                    .build();
            }
            
            // Autentica l'utente
            User user = userService.authenticateUser(
                loginRequest.getEmail().trim(),
                loginRequest.getPassword()
            );
            
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new LoginResponse("Credenziali non valide"))
                    .build();
            }
            
            // Genera il token JWT
            String token = jwtUtil.generateToken(user.getEmail(), user.getId());
            
            // Rimuovi la password dalla risposta
            user.setPassword(null);
            
            LoginResponse response = new LoginResponse(token, user);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            e.printStackTrace(); 
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new LoginResponse("Errore interno del server: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Logout 
     */
    @POST
    @Path("/logout")
    public Response logout() {
        return Response.ok()
            .entity("{\"message\": \"Logout effettuato con successo\"}")
            .build();
    }
    
    /**
     * Validità del token
     */
    @GET
    @Path("/verify")
    public Response verifyToken(@HeaderParam("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Token mancante o formato non valido\"}")
                    .build();
            }
            
            String token = authHeader.substring(7);
            
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                
                return Response.ok()
                    .entity("{\"valid\": true, \"email\": \"" + email + "\", \"userId\": " + userId + "}")
                    .build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Token non valido o scaduto\"}")
                    .build();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"message\": \"Errore nella verifica del token\"}")
                .build();
        }
    }
}