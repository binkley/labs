package lab.dynafig.spring;

import lab.dynafig.Default;
import lab.dynafig.Tracking;
import lab.dynafig.Updating;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code DynafigAutoConfiguration} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@Configuration
public class DynafigAutoConfiguration {
    private final Default dynafig = new Default();

    @Bean
    public Tracking tracking() {
        return dynafig;
    }

    @Bean
    public Updating updating() {
        return dynafig;
    }
}
