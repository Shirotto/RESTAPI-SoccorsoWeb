package it.univaq.swa.soccorsoweb.resources;

import it.univaq.swa.soccorsoweb.dto.RichiestaRequest;
import it.univaq.swa.soccorsoweb.dto.RichiestaResponse;
import it.univaq.swa.soccorsoweb.model.Richiesta_soccorso;
import it.univaq.swa.soccorsoweb.model.StatoRichiesta;
import it.univaq.swa.soccorsoweb.services.RichiestaSoccorsoService;
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
    private final JwtUtil jwtUtil = new JwtUtil();

    /**
     * Crea una nuova richiesta di soccorso
     */
    @POST
    public Response createRichiesta(RichiestaRequest richiestaRequest, 
                                  @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new RichiestaResponse("Token di autorizzazione mancante"))
                    .build();
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new RichiestaResponse("Token non valido"))
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
            richiesta.setUsersId(richiestaRequest.getUsersId());
            richiesta.setRichiedente(richiestaRequest.getRichiedente());
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
     */
    @GET
    public Response getRichiestePaginate(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("25") int size,
            @QueryParam("stato") String stato,
            @QueryParam("periodo") String periodo,
            @HeaderParam("Authorization") String authHeader) {

        try {
            // Validazione del token JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione mancante\"}")
                    .build();
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token non valido\"}")
                    .build();
            }

            // Calcola date per filtro periodo
            LocalDateTime startDate = null;
            LocalDateTime endDate = LocalDateTime.now();

            if (periodo != null) {
                switch (periodo) {
                    case "oggi":
                        startDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
                        break;
                    case "settimana":
                        startDate = LocalDateTime.now().minusWeeks(1);
                        break;
                    case "mese":
                        startDate = LocalDateTime.now().minusMonths(1);
                        break;
                }
            }

            List<Richiesta_soccorso> richieste;

            // Applica filtri
            if (stato != null && !stato.isEmpty()) {
                try {
                    StatoRichiesta statoEnum = StatoRichiesta.valueOf(stato);
                    richieste = richiestaService.findRichiesteByStato(statoEnum);
                } catch (IllegalArgumentException e) {
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Stato non valido\"}")
                        .build();
                }
            } else if (startDate != null) {
                richieste = richiestaService.findRichiesteByPeriod(startDate, endDate);
            } else {
                richieste = richiestaService.findAllRichieste();
            }

            // Simulazione paginazione lato server (idealmente faresti questo nel database)
            int totalElements = richieste.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int start = (page - 1) * size;
            int end = Math.min(start + size, totalElements);

            if (start >= totalElements) {
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
            response.put("last", page == totalPages);

            return Response.ok(response).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Errore nel recuperare le richieste\"}")
                .build();
        }
    }

    /**
     * Ottieni una richiesta per ID
     */
    @GET
    @Path("/{id}")
    public Response getRichiestaById(@PathParam("id") Long id, 
                                   @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new RichiestaResponse("Token di autorizzazione mancante"))
                    .build();
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new RichiestaResponse("Token non valido"))
                    .build();
            }

            Richiesta_soccorso richiesta = richiestaService.findRichiestaById(id);
            if (richiesta == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new RichiestaResponse("Richiesta non trovata"))
                    .build();
            }

            return Response.ok(richiesta).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new RichiestaResponse("Errore nel recuperare la richiesta"))
                .build();
        }
    }

    /**
     * Ottieni richieste per user ID
     */
    @GET
    @Path("/user/{userId}")
    public Response getRichiesteByUserId(@PathParam("userId") String userId, 
                                       @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione mancante\"}")
                    .build();
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token non valido\"}")
                    .build();
            }

            List<Richiesta_soccorso> richieste = richiestaService.findRichiesteByUserId(userId);
            return Response.ok(richieste).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Errore nel recuperare le richieste dell'utente\"}")
                .build();
        }
    }

    /**
     * Ottieni richieste per stato
     */
    @GET
    @Path("/stato/{stato}")
    public Response getRichiesteByStato(@PathParam("stato") String stato, 
                                      @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token di autorizzazione mancante\"}")
                    .build();
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token non valido\"}")
                    .build();
            }

            try {
                StatoRichiesta statoEnum = StatoRichiesta.valueOf(stato.toUpperCase());
                List<Richiesta_soccorso> richieste = richiestaService.findRichiesteByStato(statoEnum);
                return Response.ok(richieste).build();
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Stato non valido\"}")
                    .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Errore nel recuperare le richieste per stato\"}")
                .build();
        }
    }

    /**
     * Aggiorna lo stato di una richiesta
     */
    @PUT
    @Path("/{id}/stato")
    public Response updateStatoRichiesta(@PathParam("id") Long id, 
                                       @QueryParam("stato") String stato,
                                       @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new RichiestaResponse("Token di autorizzazione mancante"))
                    .build();
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new RichiestaResponse("Token non valido"))
                    .build();
            }

            if (stato == null || stato.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new RichiestaResponse("Stato è obbligatorio"))
                    .build();
            }

            try {
                StatoRichiesta statoEnum = StatoRichiesta.valueOf(stato.toUpperCase());
                Richiesta_soccorso richiesta = richiestaService.updateStatoRichiesta(id, statoEnum);
                
                if (richiesta == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity(new RichiestaResponse("Richiesta non trovata"))
                        .build();
                }

                return Response.ok(new RichiestaResponse("Stato aggiornato con successo", richiesta)).build();
                
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new RichiestaResponse("Stato non valido"))
                    .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new RichiestaResponse("Errore nell'aggiornare lo stato"))
                .build();
        }
    }

    /**
     * Elimina una richiesta
     */
    @DELETE
    @Path("/{id}")
    public Response deleteRichiesta(@PathParam("id") Long id, 
                                  @HeaderParam("Authorization") String authHeader) {
        try {
            // Validazione del token JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new RichiestaResponse("Token di autorizzazione mancante"))
                    .build();
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new RichiestaResponse("Token non valido"))
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
                .entity(new RichiestaResponse("Errore nell'eliminare la richiesta"))
                .build();
        }
    }
}