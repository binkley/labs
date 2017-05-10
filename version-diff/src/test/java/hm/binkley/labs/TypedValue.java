package hm.binkley.labs;

import hm.binkley.labs.function.ThrowingFunction;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
final class TypedValue<T> {
    public final T value;
    public final Class relatedTo;

    public static <T> TypedValue<T> of(final T value, final Class type) {
        return new TypedValue<>(value, type);
    }

    private TypedValue(final T value, final Class relatedTo) {
        this.value = value;
        this.relatedTo = relatedTo;
    }

    public <U, E extends Exception> TypedValue<U> map(
            final ThrowingFunction<? super T, ? extends U, E> then)
            throws E {
        return new TypedValue<>(then.apply(value), relatedTo);
    }
}
