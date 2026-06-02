package cn.ggsn.openrxlight;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/")
public class HealthCheckApi {

    @GET
    @Path("/healthcheck")
    public String ok() {
        return "OK";
    }
}
