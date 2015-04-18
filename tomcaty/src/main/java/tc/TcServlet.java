package tc;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * {@code TcServlet} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@WebServlet("/howdy")
public class TcServlet
        extends HttpServlet {
    @Override
    protected void doGet(final HttpServletRequest req,
            final HttpServletResponse resp)
            throws ServletException, IOException {
        resp.getWriter().println("Hello, world!");
    }
}
