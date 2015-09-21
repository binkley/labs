package lab.dynafig;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static java.util.Arrays.asList;
import static lab.dynafig.DynafigTesting.Args.params;
import static lab.dynafig.Tracking.IGNORE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code DynafigTesting} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@RequiredArgsConstructor
@RunWith(Parameterized.class)
public abstract class DynafigTesting<T, R> {
    protected static final String KEY = "bob";

    protected final Args<T, R> args;

    private Object dynafig;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> parameters() {
        return asList(Args.<String, AtomicReference<String>>params(
                "env key with string values", Tracking::track,
                AtomicReference::get, "sally", "sally", "bill", "bill", null),

                params("env key with boolean values", Tracking::trackBool,
                        AtomicBoolean::get, "true", true, "false", false,
                        false),

                params("env key with integer values", Tracking::trackInt,
                        AtomicInteger::get, "3", 3, "4", 4, 0),

                Args.<File, AtomicReference<File>>params(
                        "env key with reference type values",
                        (d, k, o) -> d.trackAs(k, File::new, o),
                        AtomicReference::get, "sally", new File("sally"),
                        "bill", new File("bill"), null));
    }

    protected <D extends Tracking & Updating> void dynafig(final D dynafig) {
        this.dynafig = dynafig;
    }

    @SuppressWarnings("unchecked")
    private <D extends Tracking & Updating> D dynafig() {
        return (D) dynafig;
    }

    protected abstract void presetValue(final String value);

    @Test
    public final void shouldNotFindMissingKey() {
        assertThat(track().isPresent(), is(false));
    }

    @Test
    public final void shouldHandleNullValue() {
        presetValue(null);

        assertThat(value(), is(equalTo(args.nullValue)));
    }

    @Test
    public final void shouldHandleNonNullValue() {
        presetValue(args.oldValue);

        assertThat(value(), is(equalTo(args.oldExepcted)));
    }

    @Test
    public final void shouldUpdateWhenKeyMissing() {
        dynafig().update(KEY, args.newValue);

        assertThat(value(), is(equalTo(args.newExepcted)));
    }

    @Test
    public final void shouldUpdateWhenKeyPresent() {
        presetValue(args.oldValue);

        dynafig().update(KEY, args.newValue);

        assertThat(value(), is(equalTo(args.newExepcted)));
    }

    @Test
    public final void shouldObserveWhenUpdatedAndKeyMissing() {
        final AtomicReference<Object> key = new AtomicReference<>();
        final AtomicReference<Object> value = new AtomicReference<>();
        args.track(dynafig(), (k, v) -> {
            key.set(k);
            value.set(v);
        });
        dynafig().update(KEY, args.newValue);

        assertThat("key", key.get(), is(equalTo(KEY)));
        assertThat("value", value.get(), is(equalTo(args.newExepcted)));
    }

    @Test
    public final void shouldObserveWhenUpdatedAndKeyPresent() {
        presetValue(args.oldValue);

        final AtomicReference<Object> key = new AtomicReference<>();
        final AtomicReference<Object> value = new AtomicReference<>();
        args.track(dynafig(), (k, v) -> {
            key.set(k);
            value.set(v);
        });
        dynafig().update(KEY, args.newValue);

        assertThat("key", key.get(), is(equalTo(KEY)));
        assertThat("value", value.get(), is(equalTo(args.newExepcted)));
    }

    protected final T value() {
        return args.value(dynafig());
    }

    private Optional<R> track() {
        return args.track(dynafig());
    }

    @FunctionalInterface
    private interface Tracker<T, R> {
        default Optional<R> track(final Tracking dynafig, final String key) {
            return track(dynafig, key, IGNORE);
        }

        Optional<R> track(final Tracking dynafig, final String key,
                final BiConsumer<String, ? super T> onUpdate);
    }

    @FunctionalInterface
    private interface Getter<T, R> {
        T get(final R atomic);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @ToString(of = "description")
    protected static final class Args<T, R> {
        public final String description;
        public final boolean keyPresent;
        public final Tracker<T, R> tracker;
        public final Getter<T, R> getter;
        public final String oldValue;
        public final T oldExepcted;
        public final String newValue;
        public final T newExepcted;
        public final T nullValue;

        static <T, R> Object[] params(final String description,
                final Tracker<T, R> tracker, final Getter<T, R> getter,
                final String oldValue, final T oldExpected,
                final String newValue, final T newExpected,
                final T nullValue) {
            return new Object[]{
                    new Args<>(description, true, tracker, getter, oldValue,
                            oldExpected, newValue, newExpected, nullValue)};
        }

        private Optional<R> track(final Tracking dynafig) {
            return tracker.track(dynafig, KEY);
        }

        private Optional<R> track(final Tracking dynafig,
                final BiConsumer<String, ? super T> onUpdate) {
            return tracker.track(dynafig, KEY, onUpdate);
        }

        private T value(final Tracking dynafig) {
            return getter.get(track(dynafig).get());
        }
    }
}
