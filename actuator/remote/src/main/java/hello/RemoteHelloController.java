package hello;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class RemoteHelloController {
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(value = "/remote-hello", method = GET)
    public Greeting remoteHello(
            @RequestParam("name") final Optional<String> name) {
        // TODO: How to do with with @Valid and ilk?
        return new Greeting(counter.incrementAndGet(),
                format("Hello, %s!", name.
                        filter(s -> !s.isEmpty()).
                        orElseThrow(RemoteHelloController::badName)));
    }

    @ExceptionHandler
    public void badArgs(final IllegalArgumentException e,
            final HttpServletResponse response)
            throws IOException {
        response.sendError(BAD_REQUEST.value(), e.getMessage());
    }

    private static IllegalArgumentException badName() {
        return new IllegalArgumentException(
                "Required String parameter 'name' is missing or empty");
    }
}
