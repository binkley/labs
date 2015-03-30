package hello;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;

@EnableFeignClients
@RestController
public class HelloWorldController {
    private final AtomicLong counter = new AtomicLong();

    private final RemoteHello gazer;

    @Inject
    public HelloWorldController(final RemoteHello gazer) {
        this.gazer = gazer;
    }

    @GET
    @RequestMapping("/hello-world")
    public Greeting sayHello(@RequestParam("name") final String name) {
        // TODO: How to do with with @Valid and ilk?
        return new Greeting(counter.incrementAndGet(),
                format("Hello, %s!", Optional.ofNullable(name).
                        filter(s -> !s.isEmpty()).
                        orElseThrow(HelloWorldController::badName)));
    }

    private static IllegalArgumentException badName() {
        return new IllegalArgumentException(
                "Required String parameter 'name' is empty");
    }

    @GET
    @RequestMapping("/navel-gaze/{name}")
    public Greeting gazeAtNavel(@PathVariable final String name) {
        return gazer.greet(name);
    }

    @ExceptionHandler
    public void badArgs(final IllegalArgumentException e,
            final HttpServletResponse response)
            throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }
}
