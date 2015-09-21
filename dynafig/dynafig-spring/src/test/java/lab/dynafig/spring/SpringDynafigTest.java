package lab.dynafig.spring;

import lab.dynafig.DynafigTesting;
import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@code SpringDynafigTest} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley</a>
 * @todo Needs documentation
 */
public class SpringDynafigTest<T, R>
        extends DynafigTesting<T, R> {
    private final Environment env = Mockito.mock(Environment.class);

    public SpringDynafigTest(final Args<T, R> args) {
        super(args);
    }

    @Before
    public void setUpFixture() {
        dynafig(new SpringDynafig(env));
    }

    @Override
    protected void presetValue(final String value) {
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(value);
    }
}
