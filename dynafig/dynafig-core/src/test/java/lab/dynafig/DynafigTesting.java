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
public abstract class DynafigTesting {
    protected static final String KEY = "bob";

    private final Args args;

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

    @Test
    public final void shouldNotFindMissingKey() {
        beforeShouldNotFindMissingKey();

        assertThat(track().isPresent(), is(false));
    }

    protected void beforeShouldNotFindMissingKey() {
    }

    @Test
    public final void shouldHandleNullValue() {
        beforeShouldHandleNullValue();

        assertThat(value(), is(equalTo(args.nullValue)));
    }

    protected void beforeShouldHandleNullValue() {
    }

    @Test
    public final void shouldHandleNonNullValue() {
        beforeShouldHandleNoNullValue();

        assertThat(value(), is(equalTo(args.oldExepcted)));
    }

    protected void beforeShouldHandleNoNullValue() {
    }

    @Test
    public final void shouldUpdateWhenKeyMissing() {
        beforeShouldUpdateWhenKeyMissing();

        dynafig().update(KEY, args.newValue);

        assertThat(value(), is(equalTo(args.newExepcted)));
    }

    protected void beforeShouldUpdateWhenKeyMissing() {
    }

    @Test
    public final void shouldUpdateWhenKeyPresent() {
        beforeShouldUpdateWhenKeyPresent();

        dynafig().update(KEY, args.newValue);

        assertThat(value(), is(equalTo(args.newExepcted)));
    }

    protected void beforeShouldUpdateWhenKeyPresent() {
    }

    @Test
    public final void shouldObserveWhenUpdatedAndKeyMissing() {
        beforeShouldObserveWhenUpdatedAndKeyMissing();

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

    protected void beforeShouldObserveWhenUpdatedAndKeyMissing() {
    }

    @Test
    public final void shouldObserveWhenUpdatedAndKeyPresent() {
        beforeShouldObserveWhenUpdatedAndKeyPresent();

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

    protected void beforeShouldObserveWhenUpdatedAndKeyPresent() {
    }

    protected final <T, R> T value() {
        return this.<T, R>args().value(dynafig());
    }

    @SuppressWarnings("unchecked")
    protected final <T, R> Args<T, R> args() {
        return (Args<T, R>) args;
    }

    private <T, R> Optional<R> track() {
        return this.<T, R>args().track(dynafig());
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
