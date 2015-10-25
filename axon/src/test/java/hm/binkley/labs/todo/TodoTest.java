package hm.binkley.labs.todo;

import hm.binkley.labs.todo.api.CompleteTodoCommand;
import hm.binkley.labs.todo.api.CreateTodoCommand;
import hm.binkley.labs.todo.annotated.Todo;
import hm.binkley.labs.todo.api.TodoCreatedEvent;
import hm.binkley.labs.todo.api.TodoDoneEvent;
import org.axonframework.test.FixtureConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.axonframework.test.Fixtures.newGivenWhenThenFixture;

public final class TodoTest {
    private static final String name = "todo1";
    private static final String content = "need to implement the aggregate";

    private FixtureConfiguration<Todo> fixture;

    @Before
    public void setUpFixture() {
        fixture = newGivenWhenThenFixture(Todo.class);
    }

    @Test
    public void testCreateTodo()
            throws Exception {
        fixture.given().
                when(CreateTodoCommand.of(name, content)).
                expectEvents(TodoCreatedEvent.of(name, content));
    }

    @Test
    public void testMarkTodoAsCompleted()
            throws Exception {
        fixture.given(TodoCreatedEvent.of(name, content)).
                when(CompleteTodoCommand.of(name)).
                expectEvents(TodoDoneEvent.of(name));
    }
}
