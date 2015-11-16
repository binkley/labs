package hm.binkley.util.concurrent;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static hm.binkley.util.concurrent.CompletionAssertJ.given;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;

public class CompletionTest {
    @Test
    public void shouldComplete()
            throws Exception {
        given(new NthAttempt(3, NeedsRetryException::new, 3)).
                whenRetrying(MILLISECONDS, 0, 10, 10, 20).
                thenAssert(CompletionTest::isDone).
                isEqualTo(3);
    }

    @Test
    public void shouldCompleteWithNullValue()
            throws Exception {
        given(new NthAttempt(null, NeedsRetryException::new, 3)).
                whenRetrying(MILLISECONDS, 0, 10, 10, 20).
                thenAssert(CompletionTest::isDone).
                isNull();
    }

    @Test(expected = TimeoutException.class)
    public void shouldTimeout()
            throws Exception {
        given(new NthAttempt(3, NeedsRetryException::new, 3)).
                whenRetrying(MILLISECONDS, 0, 10).
                thenAssert(CompletionTest::isDone);
    }

    @Test(expected = CheckedWentWrongException.class)
    public void shouldFailWithChecked()
            throws Exception {
        given(new NthAttempt(3, CheckedWentWrongException::new, 3)).
                whenRetrying(MILLISECONDS, 0).
                thenAssert(CompletionTest::isDone);
    }

    @Test(expected = UncheckedWentWrongException.class)
    public void shouldFailWithUnchecked()
            throws Exception {
        given(new NthAttempt(3, UncheckedWentWrongException::new, 3)).
                whenRetrying(MILLISECONDS, 0).
                thenAssert(CompletionTest::isDone);
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
    private static final class CheckedWentWrongException
            extends Exception {}

    @EqualsAndHashCode(callSuper = false)
    private static final class UncheckedWentWrongException
            extends RuntimeException {}

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
