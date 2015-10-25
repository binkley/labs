package hm.binkley.labs.todo;

import hm.binkley.labs.todo.api.CompleteTodoCommand;
import hm.binkley.labs.todo.api.CreateTodoCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;

import static java.util.UUID.randomUUID;

public final class CommandGenerator {
    private CommandGenerator() {}

    public static void sendCommands(final CommandGateway gateway) {
        final String todo1 = randomUUID().toString();
        final String todo2 = randomUUID().toString();

        gateway.sendAndWait(
                CreateTodoCommand.of(todo1, "Check if it really works!"));
        gateway.sendAndWait(
                CreateTodoCommand.of(todo2, "Think about the next steps!"));
        gateway.sendAndWait(CompleteTodoCommand.of(todo1));
    }
}

