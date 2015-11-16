package hm.binkley.util.concurrent;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;

import static java.lang.System.arraycopy;
import static java.lang.Thread.currentThread;
import static lombok.AccessLevel.PRIVATE;

/**
 * {@code Completion} is a retryable {@code Callable} wrapper.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
@ToString
public final class Completion<T>
        implements Callable<T> {
    /** The callable values, if available. */
    @Nullable
    public final T value;
    /** The callable failure, if available. */
    @Nullable
    public final Exception failure;

    /**
     * Creates a new {@code Completion} with the given return
     * <var>value</var>.
     *
     * @param value the value
     * @param <T> the value type
     *
     * @return the new completion, never {@code null}
     */
    @Nonnull
    public static <T> Completion<T> valued(@Nullable final T value) {
        return new Completion<>(value, null);
    }

    /**
     * Creates a new {@code Completion} with the given failure.
     *
     * @param e the failure, never {@code null}
     * @param <T> the value type
     *
     * @return the new completion, never {@code null}
     */
    @Nonnull
    public static <T> Completion<T> failed(@Nonnull final Exception e) {
        return new Completion<>(null, e);
    }

    /**
     * Attempts to complete the given <var>callable</var> until done or timeed
     * out.
     *
     * @param callable the underlying callable to complete, never {@code
     * null}
     * @param isDone the measure of when the callable is done, never {@code
     * null}
     * @param peekDelay the peek function for each retry, never {@code null}
     * @param unit the delay time unit, never {@code null}
     * @param firstDelay the amount of first delay, use 0 to try immediately
     * @param restOfDelays the amount of subsequent delays
     * @param <T> the  value type
     *
     * @return the callable result, if available
     *
     * @throws Exception if the call fails
     * @see #eventually(Callable, Predicate, TimeUnit, long, long...)
     */
    @Nullable
    public static <T> T eventually(@Nonnull final Callable<T> callable,
            @Nonnull final Predicate<Completion<T>> isDone,
            @Nonnull final LongConsumer peekDelay,
            @Nonnull final TimeUnit unit, final long firstDelay,
            final long... restOfDelays)
            throws Exception {
        return LongStream.of(delays(firstDelay, restOfDelays)).
                peek(peekDelay).
                mapToObj(delay -> delayedCall(callable, unit, delay)).
                filter(isDone).
                findFirst().
                orElseThrow(TimeoutException::new).
                call();
    }

    /**
     * Attempts to complete the given <var>callable</var> until done or timeed
     * out.
     *
     * @param callable the underlying callable to complete, never {@code
     * null}
     * @param isDone the measure of when the callable is done, never {@code
     * null}
     * @param unit the delay time unit, never {@code null}
     * @param firstDelay the amount of first delay, use 0 to try immediately
     * @param restOfDelays the amount of subsequent delays
     * @param <T> the  value type
     *
     * @return the callable result, if available
     *
     * @throws Exception if the call fails
     * @see #eventually(Callable, Predicate, LongConsumer, TimeUnit, long,
     * long...)
     */
    @Nullable
    public static <T> T eventually(@Nonnull final Callable<T> callable,
            @Nonnull final Predicate<Completion<T>> isDone,
            @Nonnull final TimeUnit unit, final long firstDelay,
            final long... restOfDelays)
            throws Exception {
        return eventually(callable, isDone, delay -> {}, unit, firstDelay,
                restOfDelays);
    }

    @Nullable
    @Override
    public T call()
            throws Exception {
        if (null == failure)
            return value;
        throw failure;
    }

    private static long[] delays(final long firstDelay,
            final long... restOfDelays) {
        final long[] delays = new long[1 + restOfDelays.length];
        delays[0] = firstDelay;
        arraycopy(restOfDelays, 0, delays, 1, restOfDelays.length);
        return delays;
    }

    private static <T> Completion<T> delayedCall(final Callable<T> callable,
            final TimeUnit unit, final long delay) {
        try {
            unit.sleep(delay);
            return valued(callable.call());
        } catch (final InterruptedException e) {
            currentThread().interrupt();
            return Completion.<T>failed(e);
        } catch (final Exception e) {
            return Completion.<T>failed(e);
        }
    }
}
