package hm.binkley.labs;

import org.axonframework.domain.EventMessage;
import org.axonframework.eventhandling.annotation.EventHandler;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class DumpingListener {
    @EventHandler
    public void onEvent(final EventMessage event) {
        out.printf("Received %s in %s on thread named %s%n",
                event.getPayload().toString(), getClass().getSimpleName(),
                currentThread().getName());
    }
}
