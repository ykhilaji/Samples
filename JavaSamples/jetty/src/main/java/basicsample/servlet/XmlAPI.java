package basicsample.servlet;

import basicsample.model.UserDAO;
import basicsample.model.entity.User;
import basicsample.model.parser.XMLParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class XmlAPI extends HttpServlet {
    private UserDAO dao;

    public XmlAPI(UserDAO dao) {
        this.dao = dao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        String idParam = req.getParameter("id");


        if (idParam != null) {
            try {
                XMLParser.jaxbMarshaller.marshal(dao.select(Long.parseLong(idParam)), writer);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        } else {
            try {
                XMLParser.jaxbMarshaller.marshal(new XMLParser.UserWrapper((List<User>) dao.selectAll()), writer);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        writer.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();

        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String email = req.getParameter("email");

        User user = new User(null, firstName, lastName, email);
        dao.insert(user);

        try {
            XMLParser.jaxbMarshaller.marshal(user, writer);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        writer.flush();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();

        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String email = req.getParameter("email");

        User user = new User(null, firstName, lastName, email);
        dao.update(user);

        try {
            XMLParser.jaxbMarshaller.marshal(user, writer);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        writer.flush();
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();

        String id = req.getParameter("id");

        if (id != null) {
            try {
                dao.delete(Long.parseLong(id));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        try {
            XMLParser.jaxbMarshaller.marshal(User.anonymous, writer);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        writer.flush();
    }
}
