package hm.binkley.labs;

import java.io.IOException;

/**
 * {@code IOFunction} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@FunctionalInterface
public interface IOFunction<T, U> {
    U apply(final T in)
            throws IOException;
}
