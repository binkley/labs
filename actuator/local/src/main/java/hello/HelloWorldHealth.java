package hello;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * {@code HelloWorldHealth} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@Component
public class HelloWorldHealth
        extends AbstractHealthIndicator {
    private final RemoteHello remote;

    @Inject
    public HelloWorldHealth(final RemoteHello remote) {
        this.remote = remote;
    }

    @Override
    protected void doHealthCheck(final Builder builder)
            throws Exception {
        builder.
                up().
                withDetail("extra", "Some details").
                withDetail("a-number", 3.14159);
    }
}
