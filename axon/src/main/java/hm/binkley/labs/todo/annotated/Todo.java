package hm.binkley.labs.todo.annotated;

import hm.binkley.labs.todo.api.CompleteTodoCommand;
import hm.binkley.labs.todo.api.CreateTodoCommand;
import hm.binkley.labs.todo.api.TodoCreatedEvent;
import hm.binkley.labs.todo.api.TodoDoneEvent;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;

@NoArgsConstructor
public final class Todo
        extends AbstractAnnotatedAggregateRoot<Todo>
        implements EventSourcedAggregateRoot<Todo> {
    @AggregateIdentifier
    private String name;
    private String content;

    @CommandHandler
    public Todo(final CreateTodoCommand command) {
        apply(TodoCreatedEvent.of(command.getName(), command.getContent()));
    }

    @EventSourcingHandler
    public void on(final TodoCreatedEvent event) {
        this.name = event.getName();
        this.content = event.getContent();
    }

    @CommandHandler
    public void markCompleted(final CompleteTodoCommand command) {
        apply(TodoDoneEvent.of(command.getName()));
    }
}
