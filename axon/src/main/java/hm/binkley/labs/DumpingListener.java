package hm.binkley.labs;

import org.axonframework.domain.EventMessage;
import org.axonframework.eventhandling.annotation.EventHandler;

/**
 * {@code ThreadPrintingEventListener} <strong>needs
 * documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public class DumpingListener {

    @EventHandler
    public void onEvent(final EventMessage event) {
        System.out.println(
                "Received " + event.getPayload().toString() + " in "
                        + getClass().getSimpleName() + " on thread named "
                        + Thread.currentThread().getName());
    }
}
