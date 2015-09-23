package lab.dynafig.spring;

import lab.dynafig.DefaultDynafig;
import lab.dynafig.Tracking;
import lab.dynafig.Updating;
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
import static java.util.Optional.ofNullable;

/**
 * {@code SpringDynafig} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public class SpringDynafig
        implements Tracking, Updating {
    private final DefaultDynafig dynafig;

    public SpringDynafig(final Environment env) {
        dynafig = new DefaultDynafig(
                key -> env.containsProperty(key) ? Optional
                        .of(ofNullable(env.getProperty(key))) : empty());
    }

    @Nonnull
    @Override
    public Optional<AtomicReference<String>> track(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super String> onUpdate) {
        return dynafig.track(key, onUpdate);
    }

    @Nonnull
    @Override
    public Optional<AtomicBoolean> trackBool(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Boolean> onUpdate) {
        return dynafig.trackBool(key, onUpdate);
    }

    @Nonnull
    @Override
    public Optional<AtomicInteger> trackInt(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Integer> onUpdate) {
        return dynafig.trackInt(key, onUpdate);
    }

    @Nonnull
    @Override
    public <T> Optional<AtomicReference<T>> trackAs(@Nonnull final String key,
            @Nonnull final Function<String, T> convert,
            @Nonnull final BiConsumer<String, ? super T> onUpdate) {
        return dynafig.trackAs(key, convert, onUpdate);
    }

    @Override
    public void update(@Nonnull final String key,
            @Nullable final String value) {
        track(key); // Lazy init from Spring
        dynafig.update(key, value);
    }
}
