package hm.binkley.labs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.lang.System.err;
import static java.text.MessageFormat.format;

@Component
@ConfigurationProperties(locations = "errors.yml", prefix = "boot-jpalooza",
        ignoreUnknownFields = false)
public class ExampleFailure {
    @Getter
    @Setter
    public static final class ErrorMessage {
        private String message;
        private int code;
    }

    @Getter
    @Setter
    private Map<String, ErrorMessage> errors;

    public void exampleFail() {
        try {
            throw new RuntimeException("I died.");
        } catch (final Exception e) {
            final ErrorMessage error = errors.get("some-problem");
            err.println(format(error.message, error.code, e));
        }
    }
}
