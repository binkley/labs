package hello;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
public class HelloWorldMetrics {
    private final Timer callTime;

    @Inject
    public HelloWorldMetrics(final MetricRegistry metrics) {
        callTime = metrics.timer("helloWorld.calls.sayHello");
    }

    @Around("execution(* hello.HelloWorldController.sayHello(String))")
    public Object aroundCall(final ProceedingJoinPoint cut)
            throws Throwable {
        final Context time = callTime.time();
        try {
            return cut.proceed();
        } finally {
            time.stop();
        }
    }
}
