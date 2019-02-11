package crud.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import crud.model.Entity;
import crud.repository.Repository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/api")
@RequestScoped
public class RestController {
    private static final ObjectMapper mapper = new ObjectMapper();
    @Inject
    private Repository<Entity, Long> repository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String findOne(@QueryParam("id") Long id) throws JsonProcessingException {
        return mapper.writeValueAsString(repository
                .findOne(id)
                .orElseThrow(() -> new WebApplicationException(String.format("Entity with id %d does not exist", id))));
    }

    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public String findAll() throws JsonProcessingException {
        return mapper.writeValueAsString(repository
                .findAll());
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String save(Entity entity) {
        try {
            return mapper.writeValueAsString(repository.save(entity));
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String update(Entity entity) {
        try {
            return mapper.writeValueAsString(repository.update(entity));
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteOne(@PathParam("id") Long id) {
        try {
            return mapper.writeValueAsString(repository.deleteById(id));
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}
