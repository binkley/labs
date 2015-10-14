package hm.binkley.labs;

import co.paralleluniverse.fibers.Fiber;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code FibersTest} tests that build is setup correctly for Fibers..
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class FibersTest {
    @Test(timeout = 1000L)
    public void shouldRunAgent()
            throws ExecutionException, InterruptedException {
        final Fiber<String> fiber = new Fiber<>(() -> "Foo!");
        fiber.start();

        assertThat(fiber.get(), is(equalTo("Foo!")));
    }
}
