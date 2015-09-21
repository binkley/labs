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
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code DefaultDynafigTest} tests {@link DefaultDynafig}.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley</a>
 */
@RunWith(Parameterized.class)
public class DefaultDynafigTest {
    @Parameter
    public Args args;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> testCases() {
        return Args.parameters();
    }

    @Test
    public void shouldConstructWithStream() {
        final DefaultDynafig dynafig = new DefaultDynafig(
                singletonMap("bob", args.oldValue).entrySet().stream());
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");

        assertThat(args.unctor.apply(bob.get()), is(args.oldExpected));
    }

    @Test
    public void shouldConstructWithProperties() {
        final Properties properties = new Properties();
        singletonMap("bob", args.oldValue).entrySet().stream().
                forEach(e -> properties
                        .setProperty(e.getKey(), e.getValue()));
        final DefaultDynafig dynafig = new DefaultDynafig(properties);
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");

        assertThat(args.unctor.apply(bob.get()), is(args.oldExpected));
    }

    @Test
    public void shouldFindNone() {
        final DefaultDynafig dynafig = new DefaultDynafig();
        final Optional<?> bob = args.ctor.apply(dynafig, "bob");

        assertThat(bob.isPresent(), is(false));
    }

    @Test
    public void shouldFindPrevious() {
        final DefaultDynafig dynafig = new DefaultDynafig(
                singletonMap("bob", args.oldValue));
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");

        assertThat(args.unctor.apply(bob.get()), is(args.oldExpected));
    }

    @Test
    public void shouldFindUpdate() {
        final DefaultDynafig dynafig = new DefaultDynafig(
                singletonMap("bob", args.oldValue));
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");
        bob.get(); // Force lazy eval
        dynafig.update("bob", args.newValue);

        assertThat(args.unctor.apply(bob.get()), is(args.newExpected));
    }

    @Test
    public void shouldCreateOnUpdateIfMissing() {
        final DefaultDynafig dynafig = new DefaultDynafig();
        dynafig.update("bob", args.oldValue);
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");

        assertThat(args.unctor.apply(bob.get()), is(args.oldExpected));
    }

    @Test
    public void shouldHandleNullValue() {
        final DefaultDynafig dynafig = new DefaultDynafig(
                singletonMap("bob", null));
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");

        assertThat(args.unctor.apply(bob.get()), is(args.nullExpected));
    }

    @Test
    public void shouldNotifyAtFirstNull() {
        final DefaultDynafig dynafig = new DefaultDynafig();
        final AtomicReference<Object> updated = new AtomicReference<>();
        dynafig.update("bob", null);
        args.ctorObserver
                .apply(dynafig, "bob", (key, value) -> updated.set(value));

        assertThat(updated.get(), is(args.nullExpected));
    }

    @Test
    public void shouldNotifyAtFirstNonNull() {
        final DefaultDynafig dynafig = new DefaultDynafig();
        final AtomicReference<Object> updated = new AtomicReference<>();
        dynafig.update("bob", args.oldValue);
        args.ctorObserver
                .apply(dynafig, "bob", (key, value) -> updated.set(value));

        assertThat(updated.get(), is(args.oldExpected));
    }

    @Test
    public void shouldBeBiConsumer() {
        final DefaultDynafig dynafig = new DefaultDynafig();
        singletonMap("bob", args.oldValue).forEach(dynafig);
        final Optional<Object> bob = args.ctor.apply(dynafig, "bob");

        assertThat(args.unctor.apply(bob.get()), is(args.oldExpected));
    }

    @Test
    public void shouldNotifyAfterUpdate() {
        final DefaultDynafig dynafig = new DefaultDynafig();
        final AtomicReference<Object> updated = new AtomicReference<>();
        dynafig.update("bob", args.oldValue);
        args.ctorObserver
                .apply(dynafig, "bob", (key, value) -> updated.set(value));
        dynafig.update("bob", args.newValue);

        assertThat(updated.get(), is(args.newExpected));
    }

    private enum Args {
        track("String reference",
                (Tracker<AtomicReference<String>>) DefaultDynafig::track,
                DefaultDynafig::track, AtomicReference::get, "apple", "apple",
                "banana", "banana", null),
        trackBool("Primitive boolean value", DefaultDynafig::trackBool,
                DefaultDynafig::trackBool, AtomicBoolean::get, "true", true,
                "false", false, false),
        trackInt("Primitive int value", DefaultDynafig::trackInt,
                DefaultDynafig::trackInt, AtomicInteger::get, "3", 3, "4", 4,
                0),
        trackAs("java.io.File reference",
                (Tracker<AtomicReference<File>>) Args::newFile, Args::newFile,
                AtomicReference::get, "apple", new File("apple"), "banana",
                new File("banana"), null);

        private final String description;
        private final Tracker<Object> ctor;
        private final CtorObserver<Object, Object> ctorObserver;
        private final Function<Object, Object> unctor;
        private final String oldValue;
        private final Object oldExpected;
        private final String newValue;
        private final Object newExpected;
        private final Object nullExpected;

        @SuppressWarnings("unchecked")
        <R, U, V> Args(final String description, final Tracker<R> ctor,
                final CtorObserver<R, U> ctorObserver,
                final Function<R, V> unctor, final String oldValue,
                final V oldExpected, final String newValue,
                final V newExpected, final Object nullExpected) {
            this.description = description;
            this.ctor = (Tracker) ctor;
            this.ctorObserver = (CtorObserver) ctorObserver;
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
                final DefaultDynafig t, final String k) {
            return t.trackAs(k, v -> null == v ? null : new File(v));
        }

        @Nonnull
        private static Optional<AtomicReference<File>> newFile(
                final DefaultDynafig t, final String k,
                final BiConsumer<String, ? super File> onUpdate) {
            return t.trackAs(k, v -> null == v ? null : new File(v),
                    onUpdate);
        }

        @Override
        public String toString() {
            return description;
        }

        @FunctionalInterface
        private interface Tracker<R> {
            Optional<R> apply(final DefaultDynafig dynafig, final String key);
        }

        @FunctionalInterface
        private interface CtorObserver<R, T> {
            Optional<R> apply(final DefaultDynafig dynafig, final String b,
                    final BiConsumer<String, ? super T> t);
        }
    }
}
