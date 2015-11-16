package hm.binkley.util.concurrent;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static hm.binkley.util.concurrent.Completion.eventually;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

public class CompletionTest {
    @Test
    public void shouldComplete()
            throws Exception {
        assertThat(eventually(new NthAttempt(3, NeedsRetryException::new, 3),
                CompletionTest::isDone, CompletionTest::peekDelay,
                MILLISECONDS, 0, 10, 10, 20)).
                isEqualTo(3);
    }

    @Test
    public void shouldCompleteWithNullValue()
            throws Exception {
        assertThat(
                eventually(new NthAttempt(null, NeedsRetryException::new, 3),
                        CompletionTest::isDone, CompletionTest::peekDelay,
                        MILLISECONDS, 0, 10, 10, 20)).
                isNull();
    }

    @Test(expected = TimeoutException.class)
    public void shouldTimeout()
            throws Exception {
        eventually(new NthAttempt(3, NeedsRetryException::new, 3),
                CompletionTest::isDone, CompletionTest::peekDelay,
                MILLISECONDS, 0, 10);
    }

    @Test(expected = WentWrongException.class)
    public void shouldFail()
            throws Exception {
        eventually(new NthAttempt(3, WentWrongException::new, 3),
                CompletionTest::isDone, MILLISECONDS, 0);
    }

    private static <T> boolean isDone(final Completion<T> completion) {
        return !(completion.failure instanceof NeedsRetryException);
    }

    private static PrintStream peekDelay(final long delay) {
        return out.printf("Sleeping %dms%n", delay);
    }

    @EqualsAndHashCode(callSuper = false)
    private static final class NeedsRetryException
            extends Exception {}

    @EqualsAndHashCode(callSuper = false)
    private static final class WentWrongException
            extends Exception {}

    @RequiredArgsConstructor(access = PRIVATE)
    private static final class NthAttempt
            implements Callable<Integer> {
        private final Integer value;
        private final Supplier<Exception> failure;
        private final int n;
        private int attempt;

        @Override
        public Integer call()
                throws Exception {
            if (n <= ++attempt)
                return value;
            throw failure.get();
        }
    }
}
