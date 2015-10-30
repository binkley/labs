package hm.binkley.labs;

import java.io.IOException;

/**
 * {@code IOConsumer} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@FunctionalInterface
public interface IOConsumer<T> {
    void accept(final T in)
            throws IOException;
}
