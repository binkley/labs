package hm.binkley.util.concurrent;

import lombok.Builder;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static hm.binkley.util.concurrent.CompletionAssert.given;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class CompletionAssertTest {
    @Test
    public void shouldComplete()
            throws Exception {
        final NthAttempt callable = NthAttempt.builder().
                attempting(3).
                returning(3).
                throwing(NeedsRetryException::new).
                build();

        given(callable).
                whenRetryingAfter(MILLISECONDS, 0, 10, 10, 20).
                thenAssertEventually(CompletionAssertTest::isDone).
                isEqualTo(3);
    }

    @Test
    public void shouldCompleteWithNullValue()
            throws Exception {
        final NthAttempt callable = NthAttempt.builder().
                attempting(3).
                returning(null).
                throwing(NeedsRetryException::new).
                build();

        given(callable).
                whenRetryingAfter(MILLISECONDS, 0, 10, 10, 20).
                thenAssertEventually(CompletionAssertTest::isDone).
                isNull();
    }

    @Test
    public void shouldNoteRetries()
            throws Exception {
        final NthAttempt callable = NthAttempt.builder().
                attempting(3).
                throwing(NeedsRetryException::new).
                build();
        final List<Long> delays = new ArrayList<>(4);

        given(callable).
                notingDelays(delays::add).
                whenRetryingAfter(MILLISECONDS, 0, 10, 10, 20).
                thenAssertEventually(CompletionAssertTest::isDone);

        assertThat(delays).isEqualTo(asList(0L, 10L, 10L));
    }

    @Test(expected = TimeoutException.class)
    public void shouldTimeout()
            throws Exception {
        final NthAttempt callable = NthAttempt.builder().
                attempting(3).
                throwing(NeedsRetryException::new).
                build();

        given(callable).
                whenRetryingAfter(MILLISECONDS, 0, 10).
                thenAssertEventually(CompletionAssertTest::isDone);
    }

    @Test(expected = CheckedWentWrongException.class)
    public void shouldFailWithChecked()
            throws Exception {
        final NthAttempt callable = NthAttempt.builder().
                attempting(2).
                throwing(CheckedWentWrongException::new).
                build();

        given(callable).
                whenRetryingAfter(MILLISECONDS, 0).
                thenAssertEventually(CompletionAssertTest::isDone);
    }

    @Test(expected = UncheckedWentWrongException.class)
    public void shouldFailWithUnchecked()
            throws Exception {
        final NthAttempt callable = NthAttempt.builder().
                attempting(2).
                throwing(UncheckedWentWrongException::new).
                build();

        given(callable).
                whenRetryingAfter(MILLISECONDS, 0).
                thenAssertEventually(CompletionAssertTest::isDone);
    }

    private static <T> boolean isDone(final Completion<T> completion) {
        return !(completion.failure instanceof NeedsRetryException);
    }

    private static final class NeedsRetryException
            extends Exception {}

    private static final class CheckedWentWrongException
            extends Exception {}

    private static final class UncheckedWentWrongException
            extends RuntimeException {}

    @Builder
    private static final class NthAttempt
            implements Callable<Integer> {
        private final int attempting;
        @Nullable
        private final Integer returning;
        @Nonnull
        private final Supplier<Exception> throwing;

        private int attempt;

        @Override
        public Integer call()
                throws Exception {
            if (attempting <= ++attempt)
                return returning;
            throw throwing.get();
        }
    }
}
