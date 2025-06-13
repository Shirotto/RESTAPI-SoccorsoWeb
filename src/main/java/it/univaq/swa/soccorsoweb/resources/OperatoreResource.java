package it.univaq.swa.soccorsoweb.resources;

import it.univaq.swa.soccorsoweb.model.Missione;
import it.univaq.swa.soccorsoweb.model.Operatore;
import it.univaq.swa.soccorsoweb.model.User;
import it.univaq.swa.soccorsoweb.services.MissioneService;
import it.univaq.swa.soccorsoweb.services.OperatoreService;
import it.univaq.swa.soccorsoweb.services.UserService;
import it.univaq.swa.soccorsoweb.security.JwtUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/operatori")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OperatoreResource {

    private final OperatoreService operatoreService = new OperatoreService();
    private final MissioneService missioneService = new MissioneService();
    private final UserService userService = new UserService();
    private final JwtUtil jwtUtil = new JwtUtil();

    /**
     * Estrae e valida l'utente dal token JWT
     */
    private User getCurrentUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return null;
        }

        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId != null) {
                return userService.findUserById(userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Crea un nuovo operatore
     * PERMESSO: Solo ADMIN
     */
    @POST
    public Response createOperatore(Operatore operatore, @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione non valido\"}")
                    .build();
            }

            // Solo gli admin possono creare operatori
            if (!currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Accesso negato. Solo gli admin possono creare operatori\"}")
                    .build();
            }

            if (operatore == null || operatore.getNome() == null || 
                operatore.getCognome() == null || operatore.getRuolo() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Dati operatore incompleti\"}")
                        .build();
            }
            
            Operatore saved = operatoreService.saveOperatore(operatore);
            return Response.status(Response.Status.CREATED).entity(saved).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Errore nella creazione dell'operatore\"}")
                    .build();
        }
    }

    /**
     * Ottieni dettagli di un operatore specifico
     * PERMESSO: Solo ADMIN
     */
    @GET
    @Path("/{id}")
    public Response getOperatore(@PathParam("id") Integer id, @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione non valido\"}")
                    .build();
            }

            // Solo gli admin possono vedere i dettagli degli operatori
            if (!currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Accesso negato. Solo gli admin possono visualizzare i dettagli degli operatori\"}")
                    .build();
            }

            Operatore operatore = operatoreService.findOperatoreById(id);
            if (operatore == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Operatore non trovato\"}")
                        .build();
            }
            return Response.ok(operatore).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Errore nel recupero dell'operatore\"}")
                    .build();
        }
    }

    /**
     * Ottieni lista degli operatori con filtro opzionale per disponibilità
     * PERMESSO: Solo ADMIN
     */
    @GET
    public Response getAllOperatori(
            @QueryParam("disponibile") Boolean disponibile,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione non valido\"}")
                    .build();
            }

            // Solo gli admin possono vedere la lista degli operatori
            if (!currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Accesso negato. Solo gli admin possono visualizzare la lista degli operatori\"}")
                    .build();
            }

            List<Operatore> operatori;
            if (disponibile != null) {
                operatori = operatoreService.findOperatoriByDisponibile(disponibile);
            } else {
                operatori = operatoreService.findAllOperatori();
            }
            return Response.ok(operatori).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Errore nel recupero degli operatori\"}")
                    .build();
        }
    }

    /**
     * Ottieni missioni di un operatore specifico
     * PERMESSO: Solo ADMIN
     */
    @GET
    @Path("/{id}/missioni")
    public Response getMissioniByOperatore(
            @PathParam("id") Integer operatoreId,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione non valido\"}")
                    .build();
            }

            // Solo gli admin possono vedere le missioni degli operatori
            if (!currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Accesso negato. Solo gli admin possono visualizzare le missioni degli operatori\"}")
                    .build();
            }

            if (operatoreId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Operatore ID mancante\"}").build();
            }
            
            List<Missione> missioni = missioneService.findMissioniByOperatorId(operatoreId.longValue());
            return Response.ok(missioni).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Errore nel recupero delle missioni dell'operatore\"}")
                    .build();
        }
    }
}