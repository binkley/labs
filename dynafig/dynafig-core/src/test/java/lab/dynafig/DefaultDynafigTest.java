package lab.dynafig;

import java.util.Collections;

/**
 * {@code DefaultDynafigTest} tests {@link DefaultDynafig}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley</a>
 */
public class DefaultDynafigTest<T, R>
        extends DynafigTesting<T, R, DefaultDynafig> {
    public DefaultDynafigTest(final Args<T, R> args) {
        super(args);
    }

    @Override
    protected void presetValue(final String value) {
        dynafig(new DefaultDynafig(Collections.singletonMap(KEY, value)));
    }
}
