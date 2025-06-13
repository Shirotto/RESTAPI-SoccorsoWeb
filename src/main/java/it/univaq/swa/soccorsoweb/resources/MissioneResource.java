package it.univaq.swa.soccorsoweb.resources;

import it.univaq.swa.soccorsoweb.model.Missione;
import it.univaq.swa.soccorsoweb.model.StatoRichiesta;
import it.univaq.swa.soccorsoweb.model.User;
import it.univaq.swa.soccorsoweb.services.MissioneService;
import it.univaq.swa.soccorsoweb.services.RichiestaSoccorsoService;
import it.univaq.swa.soccorsoweb.services.UserService;
import it.univaq.swa.soccorsoweb.security.JwtUtil;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/missions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MissioneResource {

    private final MissioneService missioneService = new MissioneService();
    private final RichiestaSoccorsoService richiestaService = new RichiestaSoccorsoService();
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
     * Crea una nuova missione
     * PERMESSO: Solo ADMIN
     */
    @POST
    public Response createMissione(Missione missione, @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione non valido\"}")
                    .build();
            }

            // Solo gli admin possono creare missioni
            if (!currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Accesso negato. Solo gli admin possono creare missioni\"}")
                    .build();
            }

            // Validazione dati missione
            if (missione == null || missione.getRequestId() == null ||
                missione.getAutistaId() == null || missione.getCaposquadraId() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Dati missione incompleti: richiesta, autista e caposquadra sono obbligatori\"}")
                    .build();
            }

            missioneService.saveMissione(missione);
            richiestaService.updateStatoRichiesta(missione.getRequestId(), StatoRichiesta.IN_CORSO);
            
            return Response.status(Response.Status.CREATED).entity(missione).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Errore nella creazione missione\"}")
                    .build();
        }
    }

    /**
     * Ottieni dettagli di una missione specifica
     * PERMESSO: Solo ADMIN
     */
    @GET
    @Path("/{id}")
    public Response getMissione(@PathParam("id") Long id, @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione non valido\"}")
                    .build();
            }

            // Solo gli admin possono vedere i dettagli delle missioni
            if (!currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Accesso negato. Solo gli admin possono visualizzare i dettagli delle missioni\"}")
                    .build();
            }

            Missione missione = missioneService.findMissioneById(id);
            if (missione == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Missione non trovata\"}")
                        .build();
            }
            return Response.ok(missione).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Errore nel recupero della missione\"}")
                    .build();
        }
    }

    /**
     * Ottieni lista delle missioni con filtri opzionali
     * PERMESSO: Solo ADMIN
     */
    @GET
    public Response getMissioni(
            @QueryParam("status") String status, 
            @QueryParam("operatoreId") Long operatoreId,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione non valido\"}")
                    .build();
            }

            // Solo gli admin possono vedere la lista delle missioni
            if (!currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Accesso negato. Solo gli admin possono visualizzare le missioni\"}")
                    .build();
            }

            if (operatoreId != null) {
                // Filtro per operatore
                return Response.ok(missioneService.findMissioniByOperatorId(operatoreId)).build();
            } else if (status != null && !status.isEmpty()) {
                // Filtro per stato
                return Response.ok(missioneService.findMissioniByStatus(status)).build();
            } else {
                // Nessun filtro, restituisci tutte le missioni
                return Response.ok(missioneService.findAllMissioni()).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Errore nel recupero delle missioni\"}")
                    .build();
        }
    }

    /**
     * Chiusura di una missione
     * PERMESSO: Solo ADMIN
     */
    @PUT
    @Path("/{id}/close")
    public Response closeMissione(
            @PathParam("id") Long id, 
            @QueryParam("livelloSuccesso") String livelloSuccesso,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione non valido\"}")
                    .build();
            }

            // Solo gli admin possono chiudere le missioni
            if (!currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Accesso negato. Solo gli admin possono chiudere le missioni\"}")
                    .build();
            }

            // Validazione livello successo
            if (livelloSuccesso == null || livelloSuccesso.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Il livello di successo è obbligatorio per chiudere una missione\"}")
                    .build();
            }

            Missione m = missioneService.closeMissione(id);         
            if (m == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Missione non trovata\"}").build();
            }
            
            // Salva livello di successo e aggiorna stato
            richiestaService.updateLivelloSuccesso(m.getRequestId(), livelloSuccesso);
            richiestaService.updateStatoRichiesta(m.getRequestId(), StatoRichiesta.CHIUSA);
            
            return Response.ok(m).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Errore nella chiusura missione\"}")
                    .build();
        }
    }
}