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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
    @Parameter(value = 0)
    public Args args;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Args.parameters();
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

    private enum Args {
        track(Tracking::track, AtomicReference::get, "apple", "apple",
                "banana", "banana", null),
        trackBool(Tracking::trackBool, AtomicBoolean::get, "true", true,
                "false", false, false),
        trackInt(Tracking::trackInt, AtomicInteger::get, "3", 3, "4", 4, 0),
        trackAs(Args::newFile, Args::getPath, "apple", "apple", "banana",
                "banana", null);

        private final BiFunction<Tracking, String, Optional<Object>> ctor;
        private final Function<Object, Object> unctor;
        private final String oldValue;
        private final Object oldExpected;
        private final String newValue;
        private final Object newExpected;
        private final Object nullExpected;

        @SuppressWarnings("unchecked")
        <T, U> Args(final BiFunction<Tracking, String, Optional<T>> ctor,
                final Function<T, U> unctor, final String oldValue,
                final U oldExpected, final String newValue,
                final U newExpected, final Object nullExpected) {
            this.nullExpected = nullExpected;
            this.ctor = (BiFunction) ctor;
            this.unctor = (Function) unctor;
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
            return t.trackAs(k, File.class,
                    v -> null == v ? null : new File(v));
        }

        private static String getPath(final AtomicReference<File> a) {
            return Optional.ofNullable(a.get()).
                    map(File::getPath).
                    orElse(null);
        }
    }
}
