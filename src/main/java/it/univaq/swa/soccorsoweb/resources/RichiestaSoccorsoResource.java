package it.univaq.swa.soccorsoweb.resources;

import it.univaq.swa.soccorsoweb.dto.RichiestaRequest;
import it.univaq.swa.soccorsoweb.dto.RichiestaResponse;
import it.univaq.swa.soccorsoweb.model.Richiesta_soccorso;
import it.univaq.swa.soccorsoweb.model.StatoRichiesta;
import it.univaq.swa.soccorsoweb.model.User;
import it.univaq.swa.soccorsoweb.model.UserRole;
import it.univaq.swa.soccorsoweb.services.RichiestaSoccorsoService;
import it.univaq.swa.soccorsoweb.services.UserService;
import it.univaq.swa.soccorsoweb.security.JwtUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime; 
import java.util.*;

@Path("/richieste")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RichiestaSoccorsoResource {

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
            // Log dell'errore se necessario
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Verifica se l'utente corrente è admin
     */
    private boolean isCurrentUserAdmin(String authHeader) {
        User currentUser = getCurrentUserFromToken(authHeader);
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Crea una nuova richiesta di soccorso
     * PERMESSO: Tutti gli utenti autenticati (UTENTE e ADMIN)
     */
    @POST
    public Response createRichiesta(RichiestaRequest richiestaRequest, 
                                  @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new RichiestaResponse("Token di autorizzazione non valido"))
                    .build();
            }

            // Validazioni input
            if (richiestaRequest == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new RichiestaResponse("Dati richiesta mancanti"))
                    .build();
            }

            if (richiestaRequest.getDescrizione() == null || richiestaRequest.getDescrizione().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new RichiestaResponse("Descrizione è obbligatoria"))
                    .build();
            }

            if (richiestaRequest.getIndirizzo() == null || richiestaRequest.getIndirizzo().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new RichiestaResponse("Indirizzo è obbligatorio"))
                    .build();
            }

            // Crea l'oggetto richiesta
            Richiesta_soccorso richiesta = new Richiesta_soccorso();
            // Imposta l'ID dell'utente che fa la richiesta
            richiesta.setUsersId(currentUser.getId());
            richiesta.setRichiedente(currentUser.getNome() + " " + currentUser.getCognome());
            richiesta.setDescrizione(richiestaRequest.getDescrizione().trim());
            richiesta.setIndirizzo(richiestaRequest.getIndirizzo().trim());
            richiesta.setTelefonoContattoRichiesta(richiestaRequest.getTelefonoContattoRichiesta());
            richiesta.setEmailContattoRichiesta(richiestaRequest.getEmailContattoRichiesta());

            // Salva la richiesta
            Richiesta_soccorso savedRichiesta = richiestaService.saveRichiesta(richiesta);

            return Response.status(Response.Status.CREATED)
                .entity(new RichiestaResponse("Richiesta di soccorso creata con successo", savedRichiesta))
                .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new RichiestaResponse("Errore nel creare la richiesta: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Ottieni tutte le richieste
     * PERMESSO: Solo ADMIN
     */
    @GET
    public Response getRichiestePaginate(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("25") int size,
            @QueryParam("stato") String stato,
            @QueryParam("periodo") String periodo,
            @HeaderParam("Authorization") String authHeader) {

        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione non valido\"}")
                    .build();
            }

            // Solo gli admin possono vedere tutte le richieste
            if (!currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Accesso negato. Solo gli admin possono visualizzare tutte le richieste\"}")
                    .build();
            }

            // Calcola date per filtro periodo
            LocalDateTime startDate = null;
            LocalDateTime endDate = LocalDateTime.now();

            if (periodo != null) {
                switch (periodo.toLowerCase()) {
                    case "oggi":
                        startDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
                        break;
                    case "settimana":
                        startDate = LocalDateTime.now().minusWeeks(1);
                        break;
                    case "mese":
                        startDate = LocalDateTime.now().minusMonths(1);
                        break;
                    default:
                        break;
                }
            }

            List<Richiesta_soccorso> richieste;

            // Applica filtri
            if (stato != null && !stato.isEmpty()) {
                try {
                    StatoRichiesta statoEnum = StatoRichiesta.valueOf(stato.toUpperCase());
                    richieste = richiestaService.findRichiesteByStato(statoEnum);
                } catch (IllegalArgumentException e) {
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Stato non valido: " + stato + "\"}")
                        .build();
                }
            } else if (startDate != null) {
                richieste = richiestaService.findRichiesteByPeriod(startDate, endDate);
            } else {
                richieste = richiestaService.findAllRichieste();
            }

            // Simulazione paginazione lato server
            int totalElements = richieste.size();
            int totalPages = (totalElements > 0) ? (int) Math.ceil((double) totalElements / size) : 0;
            int start = (page - 1) * size;
            int end = Math.min(start + size, totalElements);

            if (start >= totalElements || start < 0) {
                richieste = new ArrayList<>();
            } else {
                richieste = richieste.subList(start, end);
            }

            // Crea risposta con metadati di paginazione
            Map<String, Object> response = new HashMap<>();
            response.put("content", richieste);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("number", page - 1);
            response.put("size", size);
            response.put("first", page == 1);
            response.put("last", page >= totalPages || totalPages == 0);

            return Response.ok(response).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Errore nel recuperare le richieste: " + e.getMessage() + "\"}")
                .build();
        }
    }

    /**
     * Ottieni una richiesta per ID
     * PERMESSO: ADMIN può vedere tutte, UTENTE può vedere solo le proprie
     */
    @GET
    @Path("/{id}")
    public Response getRichiestaById(@PathParam("id") Long id, 
                                   @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new RichiestaResponse("Token di autorizzazione non valido"))
                    .build();
            }

            Richiesta_soccorso richiesta = richiestaService.findRichiestaById(id);
            if (richiesta == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new RichiestaResponse("Richiesta non trovata"))
                    .build();
            }

            // Gli utenti normali possono vedere solo le proprie richieste
            if (!currentUser.isAdmin() && !richiesta.getUsersId().equals(currentUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(new RichiestaResponse("Accesso negato. Puoi visualizzare solo le tue richieste"))
                    .build();
            }

            return Response.ok(richiesta).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new RichiestaResponse("Errore nel recuperare la richiesta: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Ottieni le proprie richieste (per utenti normali)
     * PERMESSO: Tutti gli utenti autenticati (le proprie richieste)
     */
    @GET
    @Path("/mie")
    public Response getMieRichieste(@HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione non valido\"}")
                    .build();
            }

            List<Richiesta_soccorso> richieste = richiestaService.findRichiesteByUserId(currentUser.getId());
            return Response.ok(richieste).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Errore nel recuperare le tue richieste: " + e.getMessage() + "\"}")
                .build();
        }
    }

    /**
     * Ottieni richieste per user ID
     * PERMESSO: ADMIN può vedere di tutti, UTENTE può vedere solo le proprie
     */
    @GET
    @Path("/user/{userId}")
    public Response getRichiesteByUserId(@PathParam("userId") Long userId, 
                                       @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione non valido\"}")
                    .build();
            }

            // Gli utenti normali possono vedere solo le proprie richieste
            if (!currentUser.isAdmin() && !userId.equals(currentUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Accesso negato. Puoi visualizzare solo le tue richieste\"}")
                    .build();
            }

            List<Richiesta_soccorso> richieste = richiestaService.findRichiesteByUserId(userId);
            return Response.ok(richieste).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Errore nel recuperare le richieste dell'utente: " + e.getMessage() + "\"}")
                .build();
        }
    }

    /**
     * Ottieni richieste per stato
     * PERMESSO: Solo ADMIN
     */
    @GET
    @Path("/stato/{stato}")
    public Response getRichiesteByStato(@PathParam("stato") String stato, 
                                      @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione non valido\"}")
                    .build();
            }

            // Solo gli admin possono filtrare per stato
            if (!currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Accesso negato. Solo gli admin possono filtrare per stato\"}")
                    .build();
            }

            try {
                StatoRichiesta statoEnum = StatoRichiesta.valueOf(stato.toUpperCase());
                List<Richiesta_soccorso> richieste = richiestaService.findRichiesteByStato(statoEnum);
                return Response.ok(richieste).build();
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Stato non valido: " + stato + "\"}")
                    .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Errore nel recuperare le richieste per stato: " + e.getMessage() + "\"}")
                .build();
        }
    }

     /**
     * Aggiorna lo stato di una richiesta
     * PERMESSO: Solo ADMIN
     */
    @PUT
    @Path("/{id}/stato")
    public Response updateStatoRichiesta(@PathParam("id") Long id, 
                                       @QueryParam("stato") String stato,
                                       @QueryParam("livelloSuccesso") String livelloSuccesso,
                                       @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new RichiestaResponse("Token di autorizzazione non valido"))
                    .build();
            }

            // Solo gli admin possono modificare lo stato delle richieste
            if (!currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(new RichiestaResponse("Accesso negato. Solo gli admin possono modificare lo stato"))
                    .build();
            }

            if (stato == null || stato.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new RichiestaResponse("Stato è obbligatorio"))
                    .build();
            }

            try {
                StatoRichiesta statoEnum = StatoRichiesta.valueOf(stato.toUpperCase());

                // Se lo stato è CHIUSA, il livello di successo è obbligatorio
                if (statoEnum == StatoRichiesta.CHIUSA) {
                    if (livelloSuccesso == null || livelloSuccesso.trim().isEmpty()) {
                        return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new RichiestaResponse("Il livello di successo è obbligatorio quando si chiude una richiesta"))
                            .build();
                    }
                }

                // CORREZIONE: Usa il metodo corretto con 3 parametri
                Richiesta_soccorso richiesta;
                if (statoEnum == StatoRichiesta.CHIUSA && livelloSuccesso != null) {
                    richiesta = richiestaService.updateStatoRichiesta(id, statoEnum, livelloSuccesso);
                } else {
                    richiesta = richiestaService.updateStatoRichiesta(id, statoEnum);
                }

                if (richiesta == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity(new RichiestaResponse("Richiesta non trovata"))
                        .build();
                }

                return Response.ok(new RichiestaResponse("Stato aggiornato con successo", richiesta)).build();

            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new RichiestaResponse("Stato non valido: " + stato))
                    .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new RichiestaResponse("Errore nell'aggiornare lo stato: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Elimina una richiesta
     * PERMESSO: Solo ADMIN
     */
    @DELETE
    @Path("/{id}")
    public Response deleteRichiesta(@PathParam("id") Long id, 
                                  @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token e recupero utente
            User currentUser = getCurrentUserFromToken(authHeader);
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new RichiestaResponse("Token di autorizzazione non valido"))
                    .build();
            }

            // Solo gli admin possono eliminare le richieste
            if (!currentUser.isAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(new RichiestaResponse("Accesso negato. Solo gli admin possono eliminare le richieste"))
                    .build();
            }

            boolean deleted = richiestaService.deleteRichiesta(id);
            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new RichiestaResponse("Richiesta non trovata"))
                    .build();
            }

            return Response.ok(new RichiestaResponse("Richiesta eliminata con successo")).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new RichiestaResponse("Errore nell'eliminare la richiesta: " + e.getMessage()))
                .build();
        }
    }
}