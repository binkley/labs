package hello;

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
@FeignClient(url = "http://localhost:8080")
public interface NavelGazing {
    @RequestMapping(value = "/hello-world", method = GET)
    Greeting greet(@RequestParam("name") final String name);
}
