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
public class HelloWorld
        extends AbstractHealthIndicator {
    @Override
    protected void doHealthCheck(final Builder builder)
            throws Exception {
        builder.
                up().
                withDetail("extra", "Some details").
                withDetail("a-number", 3.14159);
    }
}
