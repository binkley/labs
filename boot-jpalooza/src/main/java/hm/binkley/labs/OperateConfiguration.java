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
        private String id;
        private String message;
    }

    private String urlPattern;
    private Map<String, Map<String, ErrorMessage>> errors;

    public String messageFor(final String applicationName,
            final String errorName, final Object... params) {
        final ErrorMessage error = errors.get(applicationName).get(errorName);
        final Object[] fullParams = new Object[params.length + 2];
        arraycopy(params, 0, fullParams, 0, params.length);

        final int endOfParams = params.length;
        fullParams[endOfParams] = error.id;
        fullParams[endOfParams + 1] = String
                .format(urlPattern, applicationName, errorName);

        return MessageFormat
                .format(messageOf(error, endOfParams), fullParams);
    }

    public String messageFor(final String applicationName,
            final String errorName, final Exception e,
            final Object... params) {
        Throwable root = e;
        for (Throwable parent = root.getCause(); null != parent;
                parent = root.getCause())
            root = parent;
        return messageFor(applicationName, errorName, params) + ": " + root;
    }

    @Nonnull
    private static String messageOf(final ErrorMessage error,
            final int index) {
        final String rawMessage = error.message;
        final StringBuilder message = new StringBuilder(rawMessage);

        maybeAutomate(rawMessage, index, message);
        maybeAutomate(rawMessage, index + 1, message);

        return message.toString();
    }

    private static void maybeAutomate(final String rawMessage,
            final int index, final StringBuilder message) {
        if (!rawMessage.contains("{" + index + "}"))
            message.append(" [{").append(index).append("}]");
    }
}
