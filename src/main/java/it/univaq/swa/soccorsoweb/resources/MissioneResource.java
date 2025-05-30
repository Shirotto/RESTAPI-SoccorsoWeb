package it.univaq.swa.soccorsoweb.resources;

import it.univaq.swa.soccorsoweb.model.Missione;
import it.univaq.swa.soccorsoweb.model.StatoRichiesta;
import it.univaq.swa.soccorsoweb.services.MissioneService;
import it.univaq.swa.soccorsoweb.services.RichiestaSoccorsoService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/missions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MissioneResource {

    private final MissioneService missioneService = new MissioneService();
    private final RichiestaSoccorsoService richiestaService = new RichiestaSoccorsoService();

    @POST
    public Response createMissione(Missione missione) {
        try {
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

    @GET
    @Path("/{id}")
    public Response getMissione(@PathParam("id") Long id) {
        Missione missione = missioneService.findMissioneById(id);
        if (missione == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Missione non trovata\"}")
                    .build();
        }
        return Response.ok(missione).build();
    }
      @GET
    public Response getMissioni(@QueryParam("status") String status, @QueryParam("operatoreId") Long operatoreId) {
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
    }

    /*  B.  chiusura missione  */
    @PUT
    @Path("/{id}/close")
    public Response closeMissione(@PathParam("id") Long id) {
        try {
            Missione m = missioneService.closeMissione(id);          // vedi metodo nel service
            if (m == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Missione non trovata\"}").build();
            }
            // libera gli operatori e chiude la richiesta collegata
            richiestaService.updateStatoRichiesta(m.getRequestId(), StatoRichiesta.CHIUSA);
            return Response.ok(m).build();
        } catch (Exception e) {
            return Response.serverError()
                   .entity("{\"error\":\"Errore nella chiusura missione\"}").build();
        }
    }
}
