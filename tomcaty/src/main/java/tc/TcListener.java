package tc;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import static java.lang.String.format;

/**
 * {@code TcListener} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@WebListener
public class TcListener
        implements ServletContextListener {
    public void contextInitialized(final ServletContextEvent sce) {
        log(sce, "%s: init = %s", getClass().getName(), sce);
    }

    public void contextDestroyed(final ServletContextEvent sce) {
        log(sce, "%s: destroy = %s", getClass().getName(), sce);
    }

    private static void log(final ServletContextEvent sce,
            final String format, final Object... params) {
        sce.getServletContext().log(format(format, params));
    }
}
