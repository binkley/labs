package lab.dynafig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * {@code Default} is a simple implementation of {@link Tracking}.
 * <p>
 * <strong>NB</strong> &mdash; {@link #trackAs(String, Class, Function)
 * trackAs} requires an additional type token parameter.  This seems
 * redundant&mdash;the type should be captured by the converter function
 * parameter&mdash;but is required as Java does not support generic type
 * parameter reification, and there is not extant a hack which works with all
 * possible ways to pass in a function (i.e., method reference).  A solution
 * would eliminate the type token parameter needed as a caching key in {@link
 * Default}.  Alternatively, give up on memoizing and recompute conversion
 * from string each call.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 */
public final class Default
        implements Tracking, Updating {
    private final ConcurrentMap<String, Value> pairs
            = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<BiConsumer<String, Class<?>>>>
            notifies = new ConcurrentHashMap<>();

    public Default() {
    }

    public Default(@Nonnull final Map<String, String> pairs) {
        updateAll(pairs);
    }

    public Default(@Nonnull final Properties properties) {
        properties.
                forEach((k, v) -> update((String) k, (String) v));
    }

    @Nonnull
    @Override
    public Optional<AtomicReference<String>> track(@Nonnull final String key,
            @Nonnull
            final BiConsumer<String, ? extends Class<? super String>> nofify) {
        return Optional.ofNullable(pairs.get(key)).
                map(Value::get);
    }

    @Nonnull
    @Override
    public Optional<AtomicBoolean> trackBool(@Nonnull final String key,
            @Nonnull
            final BiConsumer<String, ? extends Class<? super Boolean>> nofify) {
        return Optional.ofNullable(pairs.get(key)).
                map(Value::getBool);
    }

    @Nonnull
    @Override
    public Optional<AtomicInteger> trackInt(@Nonnull final String key,
            @Nonnull
            final BiConsumer<String, ? extends Class<? super Integer>> nofify) {
        return Optional.ofNullable(pairs.get(key)).
                map(Value::getInt);
    }

    @Nonnull
    @Override
    public <T> Optional<AtomicReference<T>> trackAs(@Nonnull final String key,
            @Nonnull final Class<T> type,
            @Nonnull final Function<String, T> convert, @Nonnull
    final BiConsumer<String, ? extends Class<? super T>> nofify) {
        return Optional.ofNullable(pairs.get(key)).
                map(v -> v.getAs(type, convert));
    }

    @Override
    public void update(@Nonnull final String key,
            @Nullable final String value) {
        pairs.compute(key,
                (k, v) -> null == v ? new Value(value) : v.update(value));
    }

    private static final class Atomic<T> {
        private final T atomic;
        private final Consumer<String> update;

        private static Atomic<AtomicReference<String>> of(
                final String value) {
            final AtomicReference<String> atomic = new AtomicReference<>(
                    value);
            return new Atomic<>(atomic, atomic::set);
        }

        private static Atomic<AtomicBoolean> boolOf(final String value) {
            final AtomicBoolean atomic = new AtomicBoolean(
                    null == value ? false : Boolean.valueOf(value));
            return new Atomic<>(atomic,
                    v -> atomic.set(null == v ? false : Boolean.valueOf(v)));
        }

        private static Atomic<AtomicInteger> intOf(final String value) {
            final AtomicInteger atomic = new AtomicInteger(
                    null == value ? 0 : Integer.valueOf(value));
            return new Atomic<>(atomic,
                    v -> atomic.set(null == v ? 0 : Integer.valueOf(v)));
        }

        private static <T> Atomic<AtomicReference<T>> asOf(final String value,
                final Function<String, T> convert) {
            final AtomicReference<T> atomic = new AtomicReference<>(
                    convert.apply(value));
            return new Atomic<>(atomic, v -> atomic.set(convert.apply(v)));
        }

        private Atomic(final T atomic, final Consumer<String> update) {
            this.atomic = atomic;
            this.update = update;
        }

        private void update(final String value) {
            update.accept(value);
        }
    }

    private final class Value {
        private final String value;
        private final ConcurrentMap<Class<?>, Atomic<?>> values;

        private Value(final String value) {
            this(value, new ConcurrentHashMap<>(3));
        }

        private Value(final String value,
                final ConcurrentMap<Class<?>, Atomic<?>> values) {
            this.value = value;
            this.values = values;
        }

        private Value update(final String value) {
            // TODO: What happens if values updated while streaming?
            final Value newValue = new Value(value, values);
            newValue.values.values().stream().
                    forEach(a -> a.update(value));
            return newValue;
        }

        @SuppressWarnings("unchecked")
        private AtomicReference<String> get() {
            return (AtomicReference<String>) values.
                    computeIfAbsent(String.class,
                            k -> Atomic.of(value)).atomic;
        }

        private AtomicBoolean getBool() {
            return (AtomicBoolean) values.
                    computeIfAbsent(Boolean.class,
                            k -> Atomic.boolOf(value)).atomic;
        }

        private AtomicInteger getInt() {
            return (AtomicInteger) values.
                    computeIfAbsent(Integer.class,
                            k -> Atomic.intOf(value)).atomic;
        }

        @SuppressWarnings("unchecked")
        private <T> AtomicReference<T> getAs(final Class<T> type,
                final Function<String, T> convert) {
            return (AtomicReference<T>) values.
                    computeIfAbsent(type,
                            k -> Atomic.asOf(value, convert)).atomic;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (null == o || getClass() != o.getClass())
                return false;
            final Value that = (Value) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
