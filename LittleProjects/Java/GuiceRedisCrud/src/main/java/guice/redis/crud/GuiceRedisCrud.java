package guice.redis.crud;

import com.google.inject.servlet.GuiceFilter;
import guice.redis.crud.web.ContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class GuiceRedisCrud {
    public static void main(String[] args) throws Exception {
        Logger logger = LogManager.getLogger("GuiceRedisCrud");
        logger.info("Start GuiceRedisCrud");

        Server server = new Server(8080);

        ServletContextHandler contextHandler = new ServletContextHandler();

        contextHandler.addServlet(DefaultServlet.class, "/");
        contextHandler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        contextHandler.addEventListener(new ContextListener());

        server.setHandler(contextHandler);
        server.start();
        server.join();
    }
}
