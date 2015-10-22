package hm.binkley.labs;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class ToDoItemCompletedEvent {
    private final String name;
}
