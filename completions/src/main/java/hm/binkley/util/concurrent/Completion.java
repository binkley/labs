package hm.binkley.util.concurrent;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

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
 * {@code Completion} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
@ToString
public final class Completion<T>
        implements Callable<T> {
    public final T value;
    public final Exception failure;

    public static <T> Completion<T> valued(final T value) {
        return new Completion<>(value, null);
    }

    public static <T> Completion<T> failed(final Exception e) {
        return new Completion<>(null, e);
    }

    public static <T> T eventually(final Callable<T> callable,
            final Predicate<Completion<T>> isDone,
            final LongConsumer peekDelay, final TimeUnit unit,
            final long firstDelay, final long... restOfDelays)
            throws Exception {
        return LongStream.of(delays(firstDelay, restOfDelays)).
                peek(peekDelay).
                mapToObj(delay -> delayedCall(callable, unit, delay)).
                filter(isDone).
                findFirst().
                orElseThrow(TimeoutException::new).
                call();
    }

    public static <T> T eventually(final Callable<T> callable,
            final Predicate<Completion<T>> isDone, final TimeUnit unit,
            final long firstDelay, final long... delays)
            throws Exception {
        return eventually(callable, isDone, delay -> {}, unit, firstDelay,
                delays);
    }

    @Override
    public T call()
            throws Exception {
        if (null == failure)
            return value;
        throw failure;
    }

    private static long[] delays(final long firstDelay,
            final long[] restOfDelays) {
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
