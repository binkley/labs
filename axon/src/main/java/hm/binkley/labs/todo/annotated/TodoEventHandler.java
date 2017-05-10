package hm.binkley.labs.todo.annotated;

import hm.binkley.labs.todo.api.TodoCreatedEvent;
import hm.binkley.labs.todo.api.TodoDoneEvent;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.annotation.Timestamp;
import org.joda.time.DateTime;

import static java.lang.System.out;

public final class TodoEventHandler {
    @EventHandler
    public void handle(final TodoCreatedEvent event,
            @Timestamp final DateTime time) {
        out.printf("We've got something to do: %s (%s, created at %s)%n",
                event.getContent(), event.getName(),
                time.toString("d-M-y H:m"));
    }

    @EventHandler
    public void handle(final TodoDoneEvent event) {
        out.printf("We've completed the task with id %s%n", event.getName());
    }
}
