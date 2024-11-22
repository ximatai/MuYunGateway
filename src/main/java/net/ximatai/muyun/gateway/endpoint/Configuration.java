package net.ximatai.muyun.gateway.endpoint;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import java.util.Map;

@Path("/configuration")
public class Configuration {

    @GET
    @Path("/")
    public Map get() {
        return Map.of("test", 1);
    }

}
