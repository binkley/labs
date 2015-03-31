package hello;

import hello.RemoteHello.HystrixHello;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.ws.rs.GET;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class HelloWorldController {
    private final HystrixHello remote;

    @Inject
    public HelloWorldController(final HystrixHello remote) {
        this.remote = remote;
    }

    @GET
    @RequestMapping(value = "/hello-world/{name}", method = GET)
    public Greeting sayHello(@PathVariable final String name) {
        // Fake enrich the data
        final Greeting greeting = remote.greet(name);
        return new Greeting(greeting.getId() * 2, greeting.getMessage());
    }
}
