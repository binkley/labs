package hm.binkley.labs;

/**
 * {@code IOFunction} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@FunctionalInterface
public interface ThrowingFunction<T, U, E extends Exception> {
    U apply(final T in)
            throws E;
}
