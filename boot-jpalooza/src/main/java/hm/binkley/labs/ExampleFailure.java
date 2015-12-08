package hm.binkley.labs;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.System.err;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExampleFailure {
    private final OperateConfiguration operate;

    public void exampleFail() {
        try {
            throw new RuntimeException("I died.",
                    new RuntimeException("No, you died!"));
        } catch (final Exception e) {
            err.println(operate.messageFor("boot-jpalooza", "some-problem", e,
                    3));
        }
    }
}
