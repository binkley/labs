package hello;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.ws.rs.GET;

@EnableFeignClients
@RestController
public class HelloWorldController {
    private final RemoteHello remote;

    @Inject
    public HelloWorldController(final RemoteHello remote) {
        this.remote = remote;
    }

    @GET
    @RequestMapping("/hello-world/{name}")
    public Greeting sayHello(@PathVariable final String name) {
        return remote.greet(name);
    }
}
