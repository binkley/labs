package lab.dynafig;

import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code DefaultDynafigTest} tests {@link DefaultDynafig}.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley</a>
 */
public class DefaultDynafigTest<T, R>
        extends DynafigTesting<T, R> {
    private final Map<String, String> config = new HashMap<>();

    public DefaultDynafigTest(final Args<T, R> args) {
        super(args);
    }

    @Before
    public void setUpFixture() {
        dynafig(new DefaultDynafig(config));
    }

    @Override
    protected void presetValue(final String value) {
        config.put(KEY, value);
    }
}
