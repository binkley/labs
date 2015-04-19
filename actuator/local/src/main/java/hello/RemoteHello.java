package hello;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import java.util.Random;

import static java.lang.String.format;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * {@code RemoteHello} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@FeignClient("remote-hello")
public interface RemoteHello {
    @RequestMapping(value = "/remote-hello", method = GET)
    Greeting greet(@RequestParam("name") final String name);

    @Component
    class HystrixHello {
        private static final Random R = new Random();
        private final RemoteHello remote;

        @Inject
        public HystrixHello(final RemoteHello remote) {
            this.remote = remote;
        }

        @HystrixCommand(fallbackMethod = "goAway")
        public Greeting greet(final String name) {
            // 7-in-8 chance of success
            if (R.nextBoolean() || R.nextBoolean() || R.nextBoolean())
                return remote.greet(name);
            else
                throw new IllegalStateException("Bleh!");
        }

        public Greeting goAway(final String name) {
            return new Greeting(0, format("You're not welcome, %s.", name));
        }
    }
}
