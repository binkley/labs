package lab.dynafig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code DefaultTest} tests {@link Default}.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 */
@RunWith(Parameterized.class)
public class DefaultTest {
    @Parameter
    public Args args;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> testCases() {
        return Args.parameters();
    }

    @Test
    public void shouldConstructWithStream() {
        final Tracking dynafig = new Default(
                singletonMap("bob", args.oldValue).entrySet().stream());
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");

        assertThat(args.unctor.apply(bob.get()), is(args.oldExpected));
    }

    @Test
    public void shouldConstructWithProperties() {
        final Properties properties = new Properties();
        singletonMap("bob", args.oldValue).entrySet().stream().
                forEach(e -> properties.put(e.getKey(), e.getValue()));
        final Tracking dynafig = new Default(properties);
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");

        assertThat(args.unctor.apply(bob.get()), is(args.oldExpected));
    }

    @Test
    public void shouldFindNone() {
        final Tracking dynafig = new Default();
        final Optional<?> bob = args.ctor.apply(dynafig, "bob");

        assertThat(bob.isPresent(), is(false));
    }

    @Test
    public void shouldFindPrevious() {
        final Tracking dynafig = new Default(
                singletonMap("bob", args.oldValue));
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");

        assertThat(args.unctor.apply(bob.get()), is(args.oldExpected));
    }

    @Test
    public void shouldFindUpdate() {
        final Default dynafig = new Default(
                singletonMap("bob", args.oldValue));
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");
        bob.get(); // Force lazy eval
        dynafig.update("bob", args.newValue);

        assertThat(args.unctor.apply(bob.get()), is(args.newExpected));
    }

    @Test
    public void shouldCreateOnUpdateIfMissing() {
        final Default dynafig = new Default();
        dynafig.update("bob", args.oldValue);
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");

        assertThat(args.unctor.apply(bob.get()), is(args.oldExpected));
    }

    @Test
    public void shouldHandleNullValue() {
        final Tracking dynafig = new Default(singletonMap("bob", null));
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");

        assertThat(args.unctor.apply(bob.get()), is(args.nullExpected));
    }

    @Test
    public void shouldNotifyAtFirstNull() {
        final Default dynafig = new Default();
        final AtomicReference<Object> updated = new AtomicReference<>();
        dynafig.update("bob", null);
        args.ctorObserver
                .apply(dynafig, "bob", (key, value) -> updated.set(value));

        assertThat(updated.get(), is(args.nullExpected));
    }

    @Test
    public void shouldNotifyAtFirstNonNull() {
        final Default dynafig = new Default();
        final AtomicReference<Object> updated = new AtomicReference<>();
        dynafig.update("bob", args.oldValue);
        args.ctorObserver
                .apply(dynafig, "bob", (key, value) -> updated.set(value));

        assertThat(updated.get(), is(args.oldExpected));
    }

    @Test
    public void shouldBeBiConsumer() {
        final Default dynafig = new Default();
        singletonMap("bob", args.oldValue).forEach(dynafig);
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");

        assertThat(args.unctor.apply(bob.get()), is(args.oldExpected));
    }

    @Test
    public void shouldNotifyAfterUpdate() {
        final Default dynafig = new Default();
        final AtomicReference<Object> updated = new AtomicReference<>();
        dynafig.update("bob", args.oldValue);
        args.ctorObserver
                .apply(dynafig, "bob", (key, value) -> updated.set(value));
        dynafig.update("bob", args.newValue);

        assertThat(updated.get(), is(args.newExpected));
    }

    @FunctionalInterface
    private interface TriFunction<A, B, C, R> {
        R apply(final A a, final B b, final C c);
    }

    private enum Args {
        track("String reference", Tracking::track, Tracking::track,
                AtomicReference::get, "apple", "apple", "banana", "banana",
                null),
        trackBool("Primitive boolean value", Tracking::trackBool,
                Tracking::trackBool, AtomicBoolean::get, "true", true,
                "false", false, false),
        trackInt("Primitive int value", Tracking::trackInt,
                Tracking::trackInt, AtomicInteger::get, "3", 3, "4", 4, 0),
        trackAs("java.io.File reference", Args::newFile, Args::newFile,
                AtomicReference::get, "apple", new File("apple"), "banana",
                new File("banana"), null);

        private final String description;
        private final BiFunction<Tracking, String, Optional<Object>> ctor;
        private final TriFunction<Tracking, String, BiConsumer<String, Object>, Optional<Object>>
                ctorObserver;
        private final Function<Object, Object> unctor;
        private final String oldValue;
        private final Object oldExpected;
        private final String newValue;
        private final Object newExpected;
        private final Object nullExpected;

        @SuppressWarnings("unchecked")
        <T, U, V> Args(final String description,
                final BiFunction<Tracking, String, Optional<T>> ctor,
                final TriFunction<Tracking, String, BiConsumer<String, ? super U>, Optional<T>> ctorObserver,
                final Function<T, V> unctor, final String oldValue,
                final V oldExpected, final String newValue,
                final V newExpected, final Object nullExpected) {
            this.description = description;
            this.ctor = (BiFunction) ctor;
            this.ctorObserver = (TriFunction) ctorObserver;
            this.unctor = (Function) unctor;
            this.nullExpected = nullExpected;
            this.oldValue = oldValue;
            this.oldExpected = oldExpected;
            this.newValue = newValue;
            this.newExpected = newExpected;
        }

        private static Collection<Object[]> parameters() {
            return asList(values()).stream().
                    map(e -> new Object[]{e}).
                    collect(toList());
        }

        @Nonnull
        private static Optional<AtomicReference<File>> newFile(
                final Tracking t, final String k) {
            return t.trackAs(k, v -> null == v ? null : new File(v));
        }

        @Nonnull
        private static Optional<AtomicReference<File>> newFile(
                final Tracking t, final String k,
                final BiConsumer<String, ? super File> onUpdate) {
            return t.trackAs(k, v -> null == v ? null : new File(v),
                    onUpdate);
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
