package hello;

import lombok.Data;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * {@code GitHubClient} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@FeignClient("remote-hello")
public interface RemoteHello {
    @RequestMapping(value = "/remote-hello", method = GET)
    Greeting greet(@RequestParam("name") final String name);

    @RequestMapping(value = "/health", method = GET)
    Health health();

    @Data
    final class Health {
        private Status status;
    }
}
