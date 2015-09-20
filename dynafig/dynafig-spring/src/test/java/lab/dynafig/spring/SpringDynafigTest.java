package lab.dynafig.spring;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static java.util.Arrays.asList;
import static lab.dynafig.Tracking.IGNORE;
import static lab.dynafig.spring.SpringDynafigTest.Args.params;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@code SpringDynafigTest} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@RequiredArgsConstructor
@RunWith(Parameterized.class)
public class SpringDynafigTest {
    private static final String KEY = "bob";

    private final Environment env = Mockito.mock(Environment.class);
    private final SpringDynafig dynafig = new SpringDynafig(env);

    public final Args args;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> parameters() {
        return asList(Args.<String, AtomicReference<String>>params(
                "env key with string values", SpringDynafig::track,
                AtomicReference::get, "sally", "sally", "bill", "bill", null),

                params("env key with boolean values",
                        SpringDynafig::trackBool, AtomicBoolean::get, "true",
                        true, "false", false, false),

                params("env key with integer values", SpringDynafig::trackInt,
                        AtomicInteger::get, "3", 3, "4", 4, 0),

                Args.<File, AtomicReference<File>>params(
                        "env key with reference type values",
                        (d, k, o) -> d.trackAs(k, File::new, o),
                        AtomicReference::get, "sally", new File("sally"),
                        "bill", new File("bill"), null));
    }

    @Test
    public void shouldNotFindMissingKey() {
        when(env.containsProperty(eq(KEY))).thenReturn(false);

        assertThat(track().isPresent(), is(false));
    }

    @Test
    public void shouldHandleNullValue() {
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(null);

        assertThat(value(), is(equalTo(args.nullValue)));
    }

    @Test
    public void shouldHandleNonNullValue() {
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(args.oldValue);

        assertThat(value(), is(equalTo(args.oldExepcted)));
    }

    @Test
    public void shouldUpdateWhenKeyMissing() {
        when(env.containsProperty(eq(KEY))).thenReturn(false);

        dynafig.update(KEY, args.oldValue);

        assertThat(value(), is(equalTo(args.oldExepcted)));
    }

    @Test
    public void shouldUpdateWhenKeyPresent() {
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(args.oldValue);

        dynafig.update(KEY, args.newValue);

        assertThat(value(), is(equalTo(args.newExepcted)));
    }

    private <T, R> Optional<R> track() {
        return this.<T, R>args().track(dynafig);
    }

    private <T, R> T value() {
        return this.<T, R>args().value(dynafig);
    }

    @SuppressWarnings("unchecked")
    private <T, R> Args<T, R> args() {
        return (Args<T, R>) args;
    }

    @FunctionalInterface
    private interface Tracker<T, R> {
        default Optional<R> track(final SpringDynafig dynafig,
                final String key) {
            return track(dynafig, key, IGNORE);
        }

        Optional<R> track(final SpringDynafig dynafig, final String key,
                final BiConsumer<String, ? super T> onUpdate);
    }

    @FunctionalInterface
    private interface Getter<T, R> {
        T get(final R atomic);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    @ToString(of = "description")
    static final class Args<T, R> {
        private final String description;
        private final boolean keyPresent;
        private final Tracker<T, R> tracker;
        private final Getter<T, R> getter;
        private final String oldValue;
        private final T oldExepcted;
        private final String newValue;
        private final T newExepcted;
        private final T nullValue;

        static <T, R> Object[] params(final String description,
                final Tracker<T, R> tracker, final Getter<T, R> getter,
                final String oldValue, final T oldExpected,
                final String newValue, final T newExpected,
                final T nullValue) {
            return new Object[]{
                    new Args<>(description, true, tracker, getter, oldValue,
                            oldExpected, newValue, newExpected, nullValue)};
        }

        private Optional<R> track(final SpringDynafig dynafig) {
            return tracker.track(dynafig, KEY);
        }

        private Optional<R> track(final SpringDynafig dynafig,
                final BiConsumer<String, ? super T> onUpdate) {
            return tracker.track(dynafig, KEY, onUpdate);
        }

        private T value(final SpringDynafig dynafig) {
            return getter.get(track(dynafig).get());
        }
    }
}
