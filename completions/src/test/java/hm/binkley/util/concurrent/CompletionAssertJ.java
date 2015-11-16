package hm.binkley.util.concurrent;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.AbstractAssert;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static lombok.AccessLevel.PRIVATE;

/**
 * {@code CompletionAssertJ} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public final class CompletionAssertJ<T>
        extends AbstractAssert<CompletionAssertJ<T>, T> {
    private CompletionAssertJ(final T actual) {
        super(actual, CompletionAssertJ.class);
    }

    public static <T> Given<T> given(final Callable<T> callable) {
        return new Given<>(callable);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    public static final class Given<T> {
        private final Callable<T> callable;

        public WhenRetrying<T> whenRetrying(final TimeUnit unit,
                final long firstDelay, final long... restOfDelays) {
            return new WhenRetrying<>(callable, unit, firstDelay,
                    restOfDelays);
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    public static final class WhenRetrying<T> {
        private final Callable<T> callable;
        private final TimeUnit unit;
        private final long firstDelay;
        private final long[] restOfDelays;

        public CompletionAssertJ<T> thenAssert(
                final Predicate<Completion<T>> isDone)
                throws Exception {
            return new CompletionAssertJ<>(Completion
                    .eventually(callable, isDone, unit, firstDelay,
                            restOfDelays));
        }
    }
}
