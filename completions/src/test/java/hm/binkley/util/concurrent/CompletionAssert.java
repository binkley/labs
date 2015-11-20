package hm.binkley.util.concurrent;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.AbstractAssert;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;
import java.util.function.Predicate;

import static hm.binkley.util.concurrent.Completion.eventually;
import static lombok.AccessLevel.PRIVATE;

/**
 * {@code CompletionAssert} provides an AssertJ assertion which
 * <em>eventually</em> evaluates in a "given, when, then" builder pattern.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo How to specialize for specific types, e.g., File?
 */
public final class CompletionAssert<T>
        extends AbstractAssert<CompletionAssert<T>, T> {
    private CompletionAssert(final T actual) {
        super(actual, CompletionAssert.class);
    }

    /**
     * Starts an eventual assertion for the vien <var>callable</var>.
     *
     * @param callable the callable returning the asserted-on value, never
     * {@code null}
     * @param <T> the return type
     *
     * @return the next given-when-then step defining the assertion, never
     * {@code null}
     */
    @Nonnull
    public static <T> Given<T> given(@Nonnull final Callable<T> callable) {
        return new Given<>(callable);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    public static final class Given<T> {
        private final Callable<T> callable;

        public NotingDelays<T> notingDelays(final LongConsumer peekDelay) {
            return new NotingDelays<>(callable, peekDelay);
        }

        public WhenRetryingAfter<T> whenRetryingAfter(final TimeUnit unit,
                final long firstDelay, final long... restOfDelays) {
            return new WhenRetryingAfter<>(callable, delay -> {}, unit,
                    firstDelay, restOfDelays);
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    public static final class NotingDelays<T> {
        private final Callable<T> callable;
        private final LongConsumer peekDelay;

        public WhenRetryingAfter<T> whenRetryingAfter(final TimeUnit unit,
                final long firstDelay, final long... restOfDelays) {
            return new WhenRetryingAfter<>(callable, peekDelay, unit,
                    firstDelay, restOfDelays);
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    public static final class WhenRetryingAfter<T> {
        private final Callable<T> callable;
        private final LongConsumer peekDelay;
        private final TimeUnit unit;
        private final long firstDelay;
        private final long[] restOfDelays;

        public CompletionAssert<T> thenAssertEventually(
                final Predicate<Completion<T>> isDone)
                throws Exception {
            return new CompletionAssert<>(
                    eventually(callable, isDone, peekDelay, unit, firstDelay,
                            restOfDelays));
        }
    }
}
