package lab;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * {@code CountedSpliterator} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@RequiredArgsConstructor
@Accessors(fluent = true)
final class CountedSpliterator<T>
        implements Spliterator<T> {
    private final Spliterator<T> it;
    @Getter
    private int size;

    public static <T> CountedSpliterator<T> count(final Stream<T> stream) {
        return new CountedSpliterator<>(stream.spliterator());
    }

    public boolean isEmpty() {
        return 0 == size();
    }

    public boolean tryAdvance(final Consumer<? super T> action) {
        final boolean r = it.tryAdvance(action);
        if (r)
            ++size;
        return r;
    }

    public Spliterator<T> trySplit() {
        return null;
    }

    public long estimateSize() {
        return it.estimateSize();
    }

    public int characteristics() {
        return it.characteristics();
    }
}
