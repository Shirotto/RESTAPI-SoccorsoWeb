package it.univaq.swa.soccorsoweb.resources;

import it.univaq.swa.soccorsoweb.model.Missione;
import it.univaq.swa.soccorsoweb.model.Operatore;
import it.univaq.swa.soccorsoweb.services.MissioneService;
import it.univaq.swa.soccorsoweb.services.OperatoreService;
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

    @POST
    public Response createOperatore(Operatore operatore) {
        if (operatore == null || operatore.getNome() == null || operatore.getCognome() == null || operatore.getRuolo() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Dati operatore incompleti\"}")
                    .build();
        }
        Operatore saved = operatoreService.saveOperatore(operatore);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    @GET
    @Path("/{id}")
    public Response getOperatore(@PathParam("id") Integer id) {
        Operatore operatore = operatoreService.findOperatoreById(id);
        if (operatore == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Operatore non trovato\"}")
                    .build();
        }
        return Response.ok(operatore).build();
    }

    @GET
public Response getAllOperatori(@QueryParam("disponibile") Boolean disponibile) {
    List<Operatore> operatori;
    if (disponibile != null) {
        operatori = operatoreService.findOperatoriByDisponibile(disponibile);
    } else {
        operatori = operatoreService.findAllOperatori();
    }
    return Response.ok(operatori).build();
}
// NUOVO ENDPOINT PER MISSIONI DELL'OPERATORE
    @GET
    @Path("/{id}/missioni")
    public Response getMissioniByOperatore(@PathParam("id") Integer operatoreId) {
        if (operatoreId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"Operatore ID mancante\"}").build();
        }
        List<Missione> missioni = missioneService.findMissioniByOperatorId(operatoreId.longValue());
        return Response.ok(missioni).build();
    }


}
