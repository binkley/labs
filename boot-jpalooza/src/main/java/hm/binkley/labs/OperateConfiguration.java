package hm.binkley.labs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.lang.System.arraycopy;
import static java.text.MessageFormat.format;

@Component
@ConfigurationProperties(locations = "errors.yml", prefix = "operate",
        ignoreUnknownFields = false)
public class OperateConfiguration {
    @Getter
    @Setter
    public static final class ErrorMessage {
        private String message;
        private int code;
    }

    @Getter
    @Setter
    private Map<String, Map<String, ErrorMessage>> errors;

    public String alert(final String applicationName, final String errorName,
            final Object... params) {
        final ErrorMessage error = errors.get(applicationName).get(errorName);
        final Object[] fullParams = new Object[1 + params.length];
        fullParams[0] = error.code;
        arraycopy(params, 0, fullParams, 1, params.length);
        return format(error.message, fullParams);
    }
}
