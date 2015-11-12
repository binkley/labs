package hm.binkley.labs;

/**
 * {@code IOConsumer} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
    void accept(final T in)
            throws E;
}
