package br.com.bruckner;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/hello")
public class GreetingResource {

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/public")
    @Produces(MediaType.TEXT_PLAIN)
    public String publicHello() {
        return "Public OK";
    }

    @GET
    @Path("/private/admin")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("admin")
    public String helloAdmin() {
        return "Hello " + identity.getPrincipal().getName() ;
    }

    @GET
    @Path("/private/user")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("user")
    public String helloUser() {
        return "Hello " + identity.getPrincipal().getName();
    }

    @GET
    @Path("/private/viewer")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed({"viewer"})
    public String helloViewer() {
        return "Hello " + identity.getPrincipal().getName();
    }

    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public MeResponse me() {
        List<String> roles = identity.getRoles().stream().sorted().collect(Collectors.toList());
        return new MeResponse(
            identity.getPrincipal().getName(), 
            jwt.getClaim("preferred_username"),
            jwt.getClaim("email"),
            jwt.getClaim("name"),
            roles);
    }

    public static class MeResponse {
    public String username;
    public String preferredUsername;
    public String email;
    public String name;
    public List<String> roles;

    public MeResponse(String username, String preferredUsername, String email, String name, List<String> roles) {
        this.username = username;
        this.preferredUsername = preferredUsername;
        this.email = email;
        this.name = name;
        this.roles = roles;
    }
}

}
