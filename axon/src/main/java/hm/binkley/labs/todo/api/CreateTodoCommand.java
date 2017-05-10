package hm.binkley.labs.todo.api;

import lombok.Data;
import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;

@Data(staticConstructor = "of")
public final class CreateTodoCommand {
    @TargetAggregateIdentifier
    private final String name;
    private final String content;
}
