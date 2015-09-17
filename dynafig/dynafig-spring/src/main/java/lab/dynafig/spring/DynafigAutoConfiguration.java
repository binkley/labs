package lab.dynafig.spring;

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
    private SpringDynafig dynafig;

    @Inject
    public void setDynafig(final Environment env) {
        dynafig = new SpringDynafig(env);
    }

    @Bean
    public Tracking tracking() {
        return dynafig;
    }

    @Bean
    public Updating updating() {
        return dynafig;
    }
}
