package hm.binkley.labs;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class ToDoItemCreatedEvent {
    private final String name;
    private final String data;
}
