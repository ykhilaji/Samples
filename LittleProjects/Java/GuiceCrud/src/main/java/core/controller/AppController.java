package core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import core.model.User;
import core.service.UserService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
@RequestScoped
public class AppController {
    @Inject
    private ObjectMapper objectMapper;
    @Inject
    private UserService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@QueryParam("email") String email) throws JsonProcessingException {
        return objectMapper.writeValueAsString(service.get(email));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String update(User user) throws JsonProcessingException {
        return objectMapper.writeValueAsString(service.update(user));
    }

    @DELETE
    @Path("/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public String delete(@PathParam("email") String email) throws JsonProcessingException {
        return objectMapper.writeValueAsString(service.delete(email));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String create(User user) throws JsonProcessingException {
        return objectMapper.writeValueAsString(service.save(user));
    }
}
