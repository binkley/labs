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
public class SpringDynafigTest
        extends DynafigTesting {
    private final Environment env = Mockito.mock(Environment.class);

    public SpringDynafigTest(final Args args) {
        super(args);
    }

    @Before
    public void setUpFixture() {
        dynafig(new SpringDynafig(env));
    }

    @Override
    protected void beforeShouldNotFindMissingKey() {
        when(env.containsProperty(eq(KEY))).thenReturn(false);
    }

    @Override
    protected void beforeShouldHandleNullValue() {
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(null);
    }

    @Override
    protected void beforeShouldHandleNoNullValue() {
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(args().oldValue);
    }

    @Override
    protected void beforeShouldUpdateWhenKeyMissing() {
        when(env.containsProperty(eq(KEY))).thenReturn(false);
    }

    @Override
    protected void beforeShouldUpdateWhenKeyPresent() {
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(args().oldValue);
    }

    @Override
    protected void beforeShouldObserveWhenUpdatedAndKeyMissing() {
        when(env.containsProperty(eq(KEY))).thenReturn(false);
    }

    @Override
    protected void beforeShouldObserveWhenUpdatedAndKeyPresent() {
        when(env.containsProperty(eq(KEY))).thenReturn(true);
        when(env.getProperty(eq(KEY))).thenReturn(args().oldValue);
    }
}
