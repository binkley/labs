package lab.dynafig.spring;

import lab.dynafig.Tracking;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code DynafigSpringMain} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@EnableAutoConfiguration
public class DynafigSpringMain {
    public static void main(final String... args) {
        SpringApplication.run(DynafigSpringMain.class, args);
    }

    @Component
    public static class Foo {
        private final AtomicInteger x;

        @Inject
        public Foo(final Tracking dynafig) {
            this.x = dynafig.trackInt("foo").get();
        }
    }
}
