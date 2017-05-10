package hm.binkley.labs;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
@ToString
public class ToDoItemCompletedEvent {
    @Nonnull
    private final String name;
}
