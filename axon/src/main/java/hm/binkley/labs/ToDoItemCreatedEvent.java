package hm.binkley.labs;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
@ToString
public class ToDoItemCreatedEvent {
    @Nonnull
    private final String name;
    @Nonnull
    private final String data;
}
