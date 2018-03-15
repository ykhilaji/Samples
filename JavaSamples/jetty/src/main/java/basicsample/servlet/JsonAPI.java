package basicsample.servlet;


import basicsample.model.UserDAO;
import basicsample.model.entity.User;
import basicsample.model.parser.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


public class JsonAPI extends HttpServlet {
    private UserDAO dao;

    public JsonAPI(UserDAO dao) {
        this.dao = dao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        String idParam = req.getParameter("id");

        if (idParam != null) {
            try {
                long id = Long.parseLong(req.getParameter("id"));
                writer.write(JsonParser.mapper.writeValueAsString(dao.select(id)));
                writer.flush();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                writer.write("{}");
                writer.flush();
            }
        } else {
            writer.write(JsonParser.mapper.writeValueAsString(dao.selectAll()));
            writer.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();

        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String email = req.getParameter("email");

        User user = new User(null, firstName, lastName, email);
        dao.insert(user);

        writer.write(JsonParser.mapper.writeValueAsString(user));
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

        writer.write(JsonParser.mapper.writeValueAsString(user));
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

        writer.write("id");
        writer.flush();
    }
}
