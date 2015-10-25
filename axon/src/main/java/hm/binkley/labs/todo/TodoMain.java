package hm.binkley.labs.todo;

import hm.binkley.labs.todo.annotated.Todo;
import hm.binkley.labs.todo.annotated.TodoEventHandler;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventstore.EventStore;
import org.axonframework.eventstore.fs.FileSystemEventStore;
import org.axonframework.eventstore.fs.SimpleEventFileResolver;

import java.io.File;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * {@code TodoMain} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class TodoMain {
    public static void main(final String... args)
            throws InterruptedException {
        // let's start with the Command Bus
        final CommandBus commandBus = new SimpleCommandBus();

        // the CommandGateway provides a friendlier API to send commands
        final CommandGateway commandGateway = new DefaultCommandGateway(
                commandBus);

        // we'll store Events on the FileSystem, in the "events" folder
        final EventStore eventStore = new FileSystemEventStore(
                new SimpleEventFileResolver(new File("./events")));

        // a Simple Event Bus will do
        final EventBus eventBus = new SimpleEventBus();

        // we need to configure the repository
        final EventSourcingRepository<Todo> repository
                = new EventSourcingRepository<>(Todo.class, eventStore);
        repository.setEventBus(eventBus);

        // Axon needs to know that our ToDoItem Aggregate can handle commands
        AggregateAnnotationCommandHandler
                .subscribe(Todo.class, repository, commandBus);

        // We register an event listener to see which events are created
        AnnotationEventListenerAdapter
                .subscribe(new TodoEventHandler(), eventBus);

        // and let's send some Commands on the CommandBus.
        CommandGenerator.sendCommands(commandGateway);

        if (false)
            MINUTES.sleep(5); // Chance to peek with JMX
    }
}
