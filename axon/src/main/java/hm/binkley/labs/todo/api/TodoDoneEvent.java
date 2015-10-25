package hm.binkley.labs.todo.api;

import lombok.Data;

/**
 * {@code TodoDoneEvent} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@Data(staticConstructor = "of")
public final class TodoDoneEvent {
    private final String name;

    public static TodoDoneEvent of(final CompleteTodoCommand command) {
        return TodoDoneEvent.of(command.getName());
    }
}
