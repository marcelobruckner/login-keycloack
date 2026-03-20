package br.com.bruckner;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
        JsonObject resourceAccess = jwt.getClaim("resource_access");
        final List<String> clientRoles;
        if (resourceAccess != null && resourceAccess.containsKey("login-keycloak")) {
            clientRoles = resourceAccess.getJsonObject("login-keycloak")
                .getJsonArray("roles")
                .getValuesAs(JsonString.class)
                .stream()
                .map(JsonString::getString)
                .sorted()
                .collect(Collectors.toList());
        } else {
            clientRoles = List.of();
        }

        List<String> realmRoles = identity.getRoles().stream()
            .filter(r -> !clientRoles.contains(r))
            .sorted()
            .collect(Collectors.toList());

        return new MeResponse(
            identity.getPrincipal().getName(),
            jwt.getClaim("preferred_username"),
            jwt.getClaim("email"),
            jwt.getClaim("name"),
            realmRoles,
            clientRoles);
    }

    public static class MeResponse {
        public String username;
        public String preferredUsername;
        public String email;
        public String name;
        public List<String> realmRoles;
        public List<String> clientRoles;

        public MeResponse(String username, String preferredUsername, String email, String name, List<String> realmRoles, List<String> clientRoles) {
            this.username = username;
            this.preferredUsername = preferredUsername;
            this.email = email;
            this.name = name;
            this.realmRoles = realmRoles;
            this.clientRoles = clientRoles;
        }
    }

}
