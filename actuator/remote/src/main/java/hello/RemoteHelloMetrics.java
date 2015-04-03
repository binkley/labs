package hello;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * {@code HelloWorldMetrics} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 * @todo Not a fan of aspects, argument that metrics are a form of business
 * code, and should be explicitly coded for
 * @see <a href="http://kielczewski.eu/2015/01/application-metrics-with-spring-boot-actuator/">Application
 * Metrics With Spring Boot Actuator</a>
 */
@Aspect
@Component
public class RemoteHelloMetrics {
    private final CounterService counter;

    @Inject
    public RemoteHelloMetrics(final CounterService counter) {
        this.counter = counter;
    }

    @AfterReturning(
            value = "execution(* hello.RemoteHelloController.sayHello(String)) && args(name)",
            argNames = "name")
    public void afterPass(final String name) {
        counter.increment("helloWorld.calls.sayHello");
    }

    @AfterThrowing(value = "execution(* hello.RemoteHelloController.sayHello(String))",
            throwing = "e")
    public void afterFail(final Throwable e) {
        counter.increment("helloWorld.errors.sayHello");
    }
}
