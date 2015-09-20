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
import java.util.function.BiFunction;

import static java.util.Arrays.asList;
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
        return asList(
                params("env key with string values", SpringDynafig::track,
                        AtomicReference::get, "sally", "sally", null),
                params("env key with boolean values",
                        SpringDynafig::trackBool, AtomicBoolean::get, "true",
                        true, false),
                params("env key with integer values", SpringDynafig::trackInt,
                        AtomicInteger::get, "3", 3, 0),
                params("env key with reference type values",
                        (d, k) -> d.trackAs(k, File::new),
                        AtomicReference::get, "sally", new File("sally"),
                        null));
    }

    @FunctionalInterface
    private interface Getter<V, T> {
        V get(final T atomic);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    @ToString(of = "description")
    static final class Args<V, T> {
        private final String description;
        private final boolean keyPresent;
        private final BiFunction<SpringDynafig, String, Optional<T>> tracker;
        private final Getter<V, T> getter;
        private final String stringValue;
        private final V value;
        private final V nullValue;

        static <V, T> Object[] params(final String description,
                final BiFunction<SpringDynafig, String, Optional<T>> tracker,
                final Getter<V, T> getter, final String stringValue,
                final V value, final V nullValue) {
            return new Object[]{new Args<>(description, true, tracker, getter,
                    stringValue, value, nullValue)};
        }
    }

    @Test
    public void shouldNotFindMissingKey() {
        when(env.containsProperty(eq(KEY))).thenReturn(false);

        assertThat(getOptional().isPresent(), is(false));
    }

    @Test
    public void shouldFindKeyWithNullValue() {
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(null);

        assertThat(getValue(), is(equalTo(args.nullValue)));
    }

    @Test
    public void shouldFindKeywithNonNullValue() {
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(args.stringValue);

        assertThat(getValue(), is(equalTo(args.value)));
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> getOptional() {
        return ((BiFunction<SpringDynafig, String, Optional<T>>) args.tracker)
                .apply(dynafig, KEY);
    }

    @SuppressWarnings("unchecked")
    private <V, T> V getValue() {
        return ((Getter<V, T>) args.getter).get(this.<T>getOptional().get());
    }
}
