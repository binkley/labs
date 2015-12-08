package hm.binkley.labs;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static org.slf4j.LoggerFactory.getLogger;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExampleFailure {
    private final OperateConfiguration operate;

    @PostConstruct
    public void exampleFail() {
        try {
            throw new RuntimeException("I died.",
                    new RuntimeException("No, you died!"));
        } catch (final Exception e) {
            getLogger(getClass()).error(operate
                    .messageFor("boot-jpalooza", "some-problem", e, 3));
        }
    }
}
