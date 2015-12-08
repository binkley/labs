package hm.binkley.labs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.util.Map;

import static java.lang.System.arraycopy;

@Component
@ConfigurationProperties(locations = "operate.yml", prefix = "operate")
@Getter
@Setter
public class OperateConfiguration {
    @Getter
    @Setter
    public static final class ErrorMessage {
        private String message;
    }

    private String urlPattern;
    private Map<String, Map<String, ErrorMessage>> errors;

    public String messageFor(final String applicationName,
            final String errorName, final Object... params) {
        final ErrorMessage error = errors.get(applicationName).get(errorName);
        final Object[] fullParams = new Object[1 + params.length];
        fullParams[0] = String.format(urlPattern, applicationName, errorName);
        arraycopy(params, 0, fullParams, 1, params.length);
        return MessageFormat.format(messageOf(error), fullParams);
    }

    public String messageFor(final String applicationName,
            final String errorName, final Exception e,
            final Object... params) {
        Throwable root = e;
        for (Throwable parent = root.getCause(); null != parent;
                root = parent, parent = root.getCause())
            ;
        return messageFor(applicationName, errorName, params) + ": " + root;
    }

    @Nonnull
    private static String messageOf(final ErrorMessage error) {
        final String message = error.message;
        return message.contains("{0}") ? message : message + " [{0}]";
    }
}
