package hm.binkley.labs.todo.api;

import lombok.Data;

@Data(staticConstructor = "of")
public final class TodoCreatedEvent {
    private final String name;
    private final String content;

    public static TodoCreatedEvent of(final CreateTodoCommand command) {
        return TodoCreatedEvent.of(command.getName(), command.getContent());
    }
}
