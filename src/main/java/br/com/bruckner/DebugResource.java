package br.com.bruckner;

import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/debug")
public class DebugResource {
        
    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Map<String, Object> debug() {
        return jwt.getClaimNames().stream()
            .collect(Collectors.toMap(claim -> claim, claim -> jwt.getClaim(claim)));
    }
}
