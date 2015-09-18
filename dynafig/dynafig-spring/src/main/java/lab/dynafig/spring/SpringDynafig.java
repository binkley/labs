package lab.dynafig.spring;

import lab.dynafig.DefaultDynafig;
import lab.dynafig.Tracking;
import lab.dynafig.Updating;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Optional.empty;
import static lab.dynafig.spring.SpringDynafig.Tracker.asTracker;

/**
 * {@code SpringDynafig} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@RequiredArgsConstructor
public class SpringDynafig
        implements Tracking, Updating {
    private final DefaultDynafig dynafig = new DefaultDynafig();
    private final Environment env;

    private <R, T> Optional<R> track(final String key,
            final BiConsumer<String, ? super T> onUpdate,
            final Tracker<R, T> tracker) {
        final Optional<R> tracked = tracker.apply(dynafig, key, onUpdate);
        if (tracked.isPresent())
            return tracked;
        try {
            final String value = env.getRequiredProperty(key);
            dynafig.update(key, value);
            return tracker.apply(dynafig, key, onUpdate);
        } catch (final IllegalStateException e) {
            return empty();
        }
    }

    @Nonnull
    @Override
    public Optional<AtomicReference<String>> track(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super String> onUpdate) {
        return track(key, onUpdate, Tracking::track);
    }

    @Nonnull
    @Override
    public Optional<AtomicBoolean> trackBool(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Boolean> onUpdate) {
        return track(key, onUpdate, Tracking::trackBool);
    }

    @Nonnull
    @Override
    public Optional<AtomicInteger> trackInt(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Integer> onUpdate) {
        return track(key, onUpdate, Tracking::trackInt);
    }

    @Nonnull
    @Override
    public <T> Optional<AtomicReference<T>> trackAs(@Nonnull final String key,
            @Nonnull final Function<String, T> convert,
            @Nonnull final BiConsumer<String, ? super T> onUpdate) {
        return track(key, onUpdate, asTracker(convert));
    }

    @Override
    public void update(@Nonnull final String key,
            @Nullable final String value) {
        dynafig.update(key, value);
    }

    @FunctionalInterface
    interface Tracker<R, T> {
        static <T> Tracker<AtomicReference<T>, T> asTracker(
                final Function<String, T> convert) {
            return (t, k, o) -> t.trackAs(k, convert, o);
        }

        Optional<R> apply(final Tracking tracking, final String key,
                final BiConsumer<String, ? super T> onUpdate);
    }
}
