package lab.dynafig;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
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

/**
 * {@code Default} is a simple implementation of {@link Tracking}.
 * <p>
 * <strong>NB</strong> &mdash; {@link #trackAs(String, Function) trackAs}
 * requires an additional type token parameter.  This seems
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
    private final Map<String, V> vs = new ConcurrentHashMap<>();

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
            @Nonnull final BiConsumer<String, ? super String> onUpdate) {
        return track(key, V::track, onUpdate);
    }

    @Nonnull
    @Override
    public Optional<AtomicBoolean> trackBool(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Boolean> onUpdate) {
        return track(key, V::trackBool, onUpdate);
    }

    @Nonnull
    @Override
    public Optional<AtomicInteger> trackInt(@Nonnull final String key,
            @Nonnull final BiConsumer<String, ? super Integer> onUpdate) {
        return track(key, V::trackInt, onUpdate);
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
        vs.merge(key, new V(value), (a, b) -> a.update(value));
    }

    private <B, T> Optional<B> track(final String key,
            final BiFunction<V, Consumer<T>, B> fn,
            final BiConsumer<String, ? super T> onUpdate) {
        return Optional.ofNullable(vs.get(key)).
                map(v -> fn.apply(v, curry(key, onUpdate)));
    }

    private static <U> Consumer<U> curry(final String key,
            final BiConsumer<String, ? super U> onUpdate) {
        return u -> onUpdate.accept(key, u);
    }

    private static final class V {
        private final String value;
        private final List<A<?, ?>> atomics;

        private V(final String value) {
            this(value, new CopyOnWriteArrayList<>());
        }

        private V(final String value, final List<A<?, ?>> atomics) {
            this.value = value;
            this.atomics = atomics;
            atomics.stream().
                    forEach(a -> a.accept(value));
        }

        private V update(final String value) {
            return Objects.equals(this.value, value) ? this
                    : new V(value, atomics);
        }

        private AtomicReference<String> track(
                final Consumer<? super String> onUpdate) {
            final A<AtomicReference<String>, String> s = new A<>(value,
                    new AtomicReference<>(), AtomicReference::get,
                    AtomicReference::set, onUpdate);
            atomics.add(s);
            return s.atomic;
        }

        private AtomicBoolean trackBool(
                final Consumer<? super Boolean> onUpdate) {
            final A<AtomicBoolean, Boolean> b = new A<>(value,
                    new AtomicBoolean(), AtomicBoolean::get,
                    (a, v) -> a.set(null == v ? false : Boolean.valueOf(v)),
                    onUpdate);
            atomics.add(b);
            return b.atomic;
        }

        private AtomicInteger trackInt(
                final Consumer<? super Integer> onUpdate) {
            final A<AtomicInteger, Integer> i = new A<>(value,
                    new AtomicInteger(), AtomicInteger::get,
                    (a, v) -> a.set(null == v ? 0 : Integer.valueOf(v)),
                    onUpdate);
            atomics.add(i);
            return i.atomic;
        }

        private <T> AtomicReference<T> trackAs(
                final Function<? super String, T> convert,
                final Consumer<? super T> onUpdate) {
            final A<AtomicReference<T>, T> t = new A<>(value,
                    new AtomicReference<>(), AtomicReference::get,
                    (a, v) -> a.set(null == v ? null : convert.apply(v)),
                    onUpdate);
            atomics.add(t);
            return t.atomic;
        }
    }

    private static class A<T, U>
            implements Consumer<String>, Supplier<U> {
        protected final T atomic;
        private final Function<T, U> get;
        private final BiConsumer<T, String> set;

        protected A(final String value, final T atomic,
                final Function<T, U> get, final BiConsumer<T, String> set,
                final Consumer<? super U> onUpdate) {
            this.atomic = atomic;
            this.get = get;
            this.set = set.
                    andThen((a, v) -> onUpdate.accept(get.apply(a)));
            accept(value);
        }

        @Override
        public final U get() {
            return get.apply(atomic);
        }

        @Override
        public final void accept(final String value) {
            set.accept(atomic, value);
        }
    }
}
