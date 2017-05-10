package hm.binkley.labs.function;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
    void accept(final T in)
            throws E;
}
