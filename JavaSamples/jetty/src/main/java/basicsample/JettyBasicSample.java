package basicsample;

import basicsample.filter.CustomWebFilter;
import basicsample.listener.WebEventListener;
import basicsample.model.DataBase;
import basicsample.model.UserDAO;
import basicsample.model.UserDAOImpl;
import basicsample.servlet.JsonAPI;
import basicsample.servlet.XmlAPI;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class JettyBasicSample {
    public static void main(String[] args) throws Exception {
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);

        servletContextHandler.addFilter(CustomWebFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        servletContextHandler.addEventListener(new WebEventListener());

        UserDAO userDAO = new UserDAOImpl(DataBase.getInstance());
        JsonAPI jsonAPI = new JsonAPI(userDAO);
        XmlAPI xmlAPI = new XmlAPI(userDAO);

        servletContextHandler.addServlet(new ServletHolder(jsonAPI), "/json");
        servletContextHandler.addServlet(new ServletHolder(xmlAPI), "/xml");

        Server server = new Server(8080);
        server.setHandler(servletContextHandler);

        server.start();
        server.join();
    }
}
