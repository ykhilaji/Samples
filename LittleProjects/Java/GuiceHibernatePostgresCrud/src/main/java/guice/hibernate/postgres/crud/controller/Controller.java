package guice.hibernate.postgres.crud.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.servlet.RequestScoped;
import guice.hibernate.postgres.crud.model.Entity;
import guice.hibernate.postgres.crud.service.EntityService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
@RequestScoped
public class Controller {
    @Inject
    private ObjectMapper objectMapper;
    @Inject
    private EntityService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@QueryParam("id") Long id) throws JsonProcessingException {
        if (id != null) {
            return objectMapper.writeValueAsString(service.findOne(id).orElseGet(null));
        } else {
            return objectMapper.writeValueAsString(service.findAll());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String update(Entity entity) throws JsonProcessingException {
        return objectMapper.writeValueAsString(service.update(entity));
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String delete(@QueryParam("id") Long id) throws JsonProcessingException {
        if (id != null) {
            return objectMapper.writeValueAsString(service.deleteOne(id));
        } else {
            return objectMapper.writeValueAsString(service.deleteAll());
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String create(Entity entity) throws JsonProcessingException {
        return objectMapper.writeValueAsString(service.save(entity));
    }
}
