package hello;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.ws.rs.GET;
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
        return new Greeting(counter.incrementAndGet(),
                format("Hello, %s!", name));
    }

    @GET
    @RequestMapping("/navel-gaze/{name}")
    public Greeting gazeAtNavel(@PathVariable final String name) {
        return gazer.greet(name);
    }
}
