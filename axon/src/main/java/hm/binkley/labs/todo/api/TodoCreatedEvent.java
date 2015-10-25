package hm.binkley.labs.todo.api;

import lombok.Data;

@Data(staticConstructor = "of")
public final class TodoCreatedEvent {
    private final String name;
    private final String content;
}
