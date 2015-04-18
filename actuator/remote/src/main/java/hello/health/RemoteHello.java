package hello.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.stereotype.Component;

/**
 * {@code HelloWorldHealth} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@Component
public class RemoteHello
        extends AbstractHealthIndicator {
    @Override
    protected void doHealthCheck(final Builder builder)
            throws Exception {
        builder.
                up().
                withDetail("i", "Live!");
    }
}
