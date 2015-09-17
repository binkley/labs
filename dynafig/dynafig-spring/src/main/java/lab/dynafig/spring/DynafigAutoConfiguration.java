package lab.dynafig.spring;

import lab.dynafig.DefaultDynafig;
import lab.dynafig.Tracking;
import lab.dynafig.Updating;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

/**
 * {@code DynafigAutoConfiguration} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@Configuration
@ConditionalOnClass({Tracking.class, Updating.class})
public class DynafigAutoConfiguration {
    private final DefaultDynafig dynafig = new SpringDynafig();

    @Inject
    public DynafigAutoConfiguration(final Environment env) {
    }

    @Bean
    public Tracking tracking() {
        return dynafig;
    }

    @Bean
    public Updating updating() {
        return dynafig;
    }

    private static class SpringDynafig
            extends DefaultDynafig {}
}
