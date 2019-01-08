package guice.hibernate.postgres.crud;

import com.google.inject.servlet.GuiceFilter;
import guice.hibernate.postgres.crud.configuration.AppContextListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class GuiceHibernatePostgresCrud {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ServletContextHandler contextHandler = new ServletContextHandler();

        contextHandler.addServlet(DefaultServlet.class, "/");
        contextHandler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        contextHandler.addEventListener(new AppContextListener());

        server.setHandler(contextHandler);

        server.start();
        server.join();
    }
}
