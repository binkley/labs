package hm.binkley.util.concurrent;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static hm.binkley.util.concurrent.CompletionAssertJ.given;
import static java.util.Arrays.asList;
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

    @Test
    public void shouldPeekAtDelays()
            throws Exception {
        final List<Long> delays = new ArrayList<>(4);

        given(new NthAttempt(null, NeedsRetryException::new, 3)).
                peeking(delays::add).
                whenRetrying(MILLISECONDS, 0, 10, 10, 20).
                thenAssert(CompletionTest::isDone);

        Assertions.assertThat(delays).isEqualTo(asList(0L, 10L, 10L));
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
