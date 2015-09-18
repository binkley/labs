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

    @Before
    public void setUpFixture() {
        dynafig = new SpringDynafig(env);
    }

    @Test
    public void shouldNotFindMissingEnvKeyForString() {
        final String key = "bob";
        when(env.containsProperty(eq(key))).thenReturn(false);

        assertThat(dynafig.track(key).isPresent(), is(false));
    }

    @Test
    public void shouldNotFindMissingEnvKeyForBool() {
        final String key = "bob";
        when(env.containsProperty(eq(key))).thenReturn(false);

        assertThat(dynafig.trackBool(key).isPresent(), is(false));
    }

    @Test
    public void shouldNotFindMissingEnvKeyForInt() {
        final String key = "bob";
        when(env.containsProperty(eq(key))).thenReturn(false);

        assertThat(dynafig.trackInt(key).isPresent(), is(false));
    }

    @Test
    public void shouldNotFindMissingEnvKeyForRefType() {
        final String key = "bob";
        when(env.containsProperty(eq(key))).thenReturn(false);

        assertThat(dynafig.trackAs(key, File::new).isPresent(), is(false));
    }

    @Test
    public void shouldFindEnvKeyWithStringNullValue() {
        final String key = "bob";
        final String value = null;
        when(env.containsProperty(eq(key))).thenReturn(true);
        when(env.getProperty(eq(key))).thenReturn(value);

        assertThat(dynafig.track(key).get().get(), is(nullValue()));
    }

    @Test
    public void shouldFindEnvKeyWithBoolNullValueAsFalse() {
        final String key = "bob";
        when(env.containsProperty(eq(key))).thenReturn(true);
        when(env.getProperty(eq(key))).thenReturn(null);

        assertThat(dynafig.trackBool(key).get().get(), is(false));
    }

    @Test
    public void shouldFindEnvKeyWithIntNullValueAsZero() {
        final String key = "bob";
        when(env.containsProperty(eq(key))).thenReturn(true);
        when(env.getProperty(eq(key))).thenReturn(null);

        assertThat(dynafig.trackInt(key).get().get(), is(equalTo(0)));
    }

    @Test
    public void shouldFindEnvKeyWithRefTypeNullValue() {
        final String key = "bob";
        final String value = null;
        when(env.containsProperty(eq(key))).thenReturn(true);
        when(env.getProperty(eq(key))).thenReturn(value);

        assertThat(dynafig.trackAs(key, File::new).get().get(),
                is(nullValue()));
    }

    @Test
    public void shouldFindEnvKeyWithStringValue() {
        final String key = "bob";
        final String value = "sally";
        when(env.containsProperty(eq(key))).thenReturn(true);
        when(env.getProperty(eq(key))).thenReturn(value);

        assertThat(dynafig.track(key).get().get(), is(value));
    }

    @Test
    public void shouldFindEnvKeyWithBoolValue() {
        final String key = "bob";
        final boolean value = true;
        when(env.containsProperty(eq(key))).thenReturn(true);
        when(env.getProperty(eq(key))).thenReturn(String.valueOf(value));

        assertThat(dynafig.trackBool(key).get().get(), is(value));
    }

    @Test
    public void shouldFindEnvKeyWithIntValue() {
        final String key = "bob";
        final int value = 3;
        when(env.containsProperty(eq(key))).thenReturn(true);
        when(env.getProperty(eq(key))).thenReturn(String.valueOf(value));

        assertThat(dynafig.trackInt(key).get().get(), is(value));
    }

    @Test
    public void shouldFindEnvKeyWithRefTypeValue() {
        final String key = "bob";
        final File value = new File(".");
        when(env.containsProperty(eq(key))).thenReturn(true);
        when(env.getProperty(eq(key))).thenReturn(String.valueOf(value));

        assertThat(dynafig.trackAs(key, File::new).get().get(), is(value));
    }
}
