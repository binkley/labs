package hello;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
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
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@EnableFeignClients
@RestController
public class HelloWorldController {
    private final AtomicLong counter = new AtomicLong();

    private final RemoteHello remote;

    @Inject
    public HelloWorldController(final RemoteHello remote) {
        this.remote = remote;
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

    @GET
    @RequestMapping("/remote-hello/{name}")
    public Greeting remoteHello(@PathVariable final String name) {
        return remote.greet(name);
    }

    @ExceptionHandler
    public void badArgs(final IllegalArgumentException e,
            final HttpServletResponse response)
            throws IOException {
        response.sendError(BAD_REQUEST.value(), e.getMessage());
    }

    private static IllegalArgumentException badName() {
        return new IllegalArgumentException(
                "Required String parameter 'name' is empty");
    }
}
