package lab.dynafig;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * {@code DefaultDynafig} is a simple implementation of {@link Tracking}.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley</a>
 */
public class DefaultDynafig
        implements Tracking, Updating {
    private final Map<String, Value> values = new ConcurrentHashMap<>();

    public DefaultDynafig() {
    }

    public DefaultDynafig(@Nonnull final Map<String, String> pairs) {
        updateAll(pairs);
    }

    public DefaultDynafig(
            @Nonnull final Stream<Entry<String, String>> pairs) {
        pairs.forEach(pair -> update(pair.getKey(), pair.getValue()));
    }

    public DefaultDynafig(@Nonnull final Properties properties) {
        properties.forEach((k, v) -> update((String) k, (String) v));
    }

    @Nonnull
    @Override
    public Optional<AtomicReference<String>> track(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super String> onUpdate) {
        return track(key, Value::track, onUpdate);
    }

    @Nonnull
    @Override
    public Optional<AtomicBoolean> trackBool(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Boolean> onUpdate) {
        return track(key, Value::trackBool, onUpdate);
    }

    @Nonnull
    @Override
    public Optional<AtomicInteger> trackInt(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Integer> onUpdate) {
        return track(key, Value::trackInt, onUpdate);
    }

    @Nonnull
    @Override
    public <T> Optional<AtomicReference<T>> trackAs(@Nonnull final String key,
            @Nonnull final Function<String, T> convert,
            @Nonnull final BiConsumer<String, ? super T> onUpdate) {
        return track(key, (v, c) -> v.trackAs(convert, c), onUpdate);
    }

    @Override
    public void update(@Nonnull final String key, final String value) {
        values.merge(key, new Value(value), (a, b) -> a.update(value));
    }

    private <T, R> Optional<R> track(final String key,
            final BiFunction<Value, Consumer<T>, R> fn,
            final BiConsumer<String, ? super T> onUpdate) {
        return ofNullable(values.get(key)).
                map(v -> fn.apply(v, curry(key, onUpdate)));
    }

    private static <U> Consumer<U> curry(final String key,
            final BiConsumer<String, ? super U> onUpdate) {
        return u -> onUpdate.accept(key, u);
    }

    private static final class Value {
        private final String value;
        private final List<Atomic<?, ?>> atomics;

        private Value(final String value) {
            this(value, new CopyOnWriteArrayList<>());
        }

        private Value(final String value, final List<Atomic<?, ?>> atomics) {
            this.value = value;
            this.atomics = atomics;
            atomics.stream().
                    forEach(a -> a.accept(value));
        }

        private Value update(final String value) {
            return Objects.equals(this.value, value) ? this
                    : new Value(value, atomics);
        }

        private AtomicReference<String> track(
                final Consumer<? super String> onUpdate) {
            final Atomic<String, AtomicReference<String>> s = new Atomic<>(
                    value, new AtomicReference<>(), AtomicReference::get,
                    AtomicReference::set, onUpdate);
            atomics.add(s);
            return s.atomic;
        }

        private AtomicBoolean trackBool(
                final Consumer<? super Boolean> onUpdate) {
            final Atomic<Boolean, AtomicBoolean> b = new Atomic<>(value,
                    new AtomicBoolean(), AtomicBoolean::get,
                    (a, v) -> a.set(null == v ? false : Boolean.valueOf(v)),
                    onUpdate);
            atomics.add(b);
            return b.atomic;
        }

        private AtomicInteger trackInt(
                final Consumer<? super Integer> onUpdate) {
            final Atomic<Integer, AtomicInteger> i = new Atomic<>(value,
                    new AtomicInteger(), AtomicInteger::get,
                    (a, v) -> a.set(null == v ? 0 : Integer.valueOf(v)),
                    onUpdate);
            atomics.add(i);
            return i.atomic;
        }

        private <T> AtomicReference<T> trackAs(
                final Function<? super String, T> convert,
                final Consumer<? super T> onUpdate) {
            final Atomic<T, AtomicReference<T>> t = new Atomic<>(value,
                    new AtomicReference<>(), AtomicReference::get,
                    (a, v) -> a.set(null == v ? null : convert.apply(v)),
                    onUpdate);
            atomics.add(t);
            return t.atomic;
        }
    }

    private static class Atomic<T, R>
            implements Consumer<String>, Supplier<T> {
        protected final R atomic;
        private final Function<R, T> getter;
        private final BiConsumer<R, String> setter;

        protected Atomic(final String value, final R atomic,
                final Function<R, T> getter,
                final BiConsumer<R, String> setter,
                final Consumer<? super T> onUpdate) {
            this.atomic = atomic;
            this.getter = getter;
            this.setter = setter.
                    andThen((a, v) -> onUpdate.accept(getter.apply(a)));
            accept(value);
        }

        @Override
        public final T get() {
            return getter.apply(atomic);
        }

        @Override
        public final void accept(final String value) {
            setter.accept(atomic, value);
        }
    }
}
