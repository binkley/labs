package hm.binkley.labs;

import co.paralleluniverse.fibers.Fiber;

import java.util.concurrent.ExecutionException;

import static java.lang.System.out;

/**
 * {@code FibersMain} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class FibersMain {
    public static void main(final String... args)
            throws ExecutionException, InterruptedException {
        final Fiber<String> fiber = new Fiber<>(() -> "Foo!");
        fiber.start();
        out.println(fiber.get());
    }
}
