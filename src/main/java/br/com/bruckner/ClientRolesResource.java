package br.com.bruckner;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/client-roles")
public class ClientRolesResource {
    
    @Inject
    SecurityIdentity identity;


    @GET
    @Path("/private/read")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("read")
    public String helloRead() {
        return "Hello reader " + identity.getPrincipal().getName();
    }

    @GET
    @Path("/private/write")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("write")
    public String helloWrite() {
        return "Hello writer " + identity.getPrincipal().getName();
    }

}
