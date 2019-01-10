package guice.redis.crud.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import guice.redis.crud.model.Entity;
import guice.redis.crud.repository.Repository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@Path("/api")
@RequestScoped
public class RestController {
    private static final Logger logger = LogManager.getLogger("rest-controller");
    @Inject
    Repository<Entity, Long> repository;
    @Inject
    ObjectMapper mapper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String findOne(@QueryParam("id") Long id) {
        try {
            Optional<Entity> r = repository.findOne(id);
            if (r.isPresent()) {
                return mapper.writeValueAsString(r.get());
            } else {
                throw new NotFoundException(String.format("Entity with id: %d does not exist", id));
            }
        } catch (NotFoundException e) {
            logger.error(e.getLocalizedMessage());
            throw e;
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            throw new WebApplicationException(e);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String save(Entity entity) {
        try {
            return mapper.writeValueAsString(repository.save(entity));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
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
            logger.error(e.getLocalizedMessage());
            throw new WebApplicationException(e);
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteOne(@PathParam("id") Long id) {
        try {
            repository.deleteOne(id);
            return "ok";
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            throw new WebApplicationException(e);
        }
    }
}
