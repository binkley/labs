package hm.binkley.labs.function;

@FunctionalInterface
public interface ThrowingFunction<T, U, E extends Exception> {
    U apply(final T in)
            throws E;
}
