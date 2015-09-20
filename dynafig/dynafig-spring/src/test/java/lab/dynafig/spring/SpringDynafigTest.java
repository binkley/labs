package lab.dynafig.spring;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.io.File;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@code SpringDynafigTest} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@RunWith(MockitoJUnitRunner.class)
public class SpringDynafigTest {
    @Mock
    private Environment env;

    private SpringDynafig dynafig;
    public static final String KEY = "bob";

    @Before
    public void setUpFixture() {
        dynafig = new SpringDynafig(env);
    }

    @Test
    public void shouldNotFindMissingEnvKeyForString() {
        when(env.containsProperty(eq(KEY))).thenReturn(false);

        assertThat(dynafig.track(KEY).isPresent(), is(false));
    }

    @Test
    public void shouldNotFindMissingEnvKeyForBool() {
        when(env.containsProperty(eq(KEY))).thenReturn(false);

        assertThat(dynafig.trackBool(KEY).isPresent(), is(false));
    }

    @Test
    public void shouldNotFindMissingEnvKeyForInt() {
        when(env.containsProperty(eq(KEY))).thenReturn(false);

        assertThat(dynafig.trackInt(KEY).isPresent(), is(false));
    }

    @Test
    public void shouldNotFindMissingEnvKeyForRefType() {
        when(env.containsProperty(eq(KEY))).thenReturn(false);

        assertThat(dynafig.trackAs(KEY, File::new).isPresent(), is(false));
    }

    @Test
    public void shouldFindEnvKeyWithStringNullValue() {
        final String value = null;
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(value);

        assertThat(dynafig.track(KEY).get().get(), is(nullValue()));
    }

    @Test
    public void shouldFindEnvKeyWithBoolNullValueAsFalse() {
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(null);

        assertThat(dynafig.trackBool(KEY).get().get(), is(false));
    }

    @Test
    public void shouldFindEnvKeyWithIntNullValueAsZero() {
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(null);

        assertThat(dynafig.trackInt(KEY).get().get(), is(equalTo(0)));
    }

    @Test
    public void shouldFindEnvKeyWithRefTypeNullValue() {
        final String value = null;
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(value);

        assertThat(dynafig.trackAs(KEY, File::new).get().get(),
                is(nullValue()));
    }

    @Test
    public void shouldFindEnvKeyWithStringValue() {
        final String value = "sally";
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(value);

        assertThat(dynafig.track(KEY).get().get(), is(value));
    }

    @Test
    public void shouldFindEnvKeyWithBoolValue() {
        final boolean value = true;
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(String.valueOf(value));

        assertThat(dynafig.trackBool(KEY).get().get(), is(value));
    }

    @Test
    public void shouldFindEnvKeyWithIntValue() {
        final int value = 3;
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(String.valueOf(value));

        assertThat(dynafig.trackInt(KEY).get().get(), is(value));
    }

    @Test
    public void shouldFindEnvKeyWithRefTypeValue() {
        final File value = new File("sally");
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(String.valueOf(value));

        assertThat(dynafig.trackAs(KEY, File::new).get().get(), is(value));
    }
}
