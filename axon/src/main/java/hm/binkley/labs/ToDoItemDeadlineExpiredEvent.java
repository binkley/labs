package hm.binkley.labs;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class ToDoItemDeadlineExpiredEvent {
    private final String name;
}
