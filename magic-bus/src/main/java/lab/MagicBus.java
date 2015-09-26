package lab;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * {@code MagicBus} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class MagicBus {
    private final Map<Class, Set<Consumer>> receivers
            = new ConcurrentHashMap<>();

    public <T> void subscribe(final Class<T> eventType,
            final Consumer<? super T> receiver) {
        receivers.computeIfAbsent(eventType, MagicBus::newReceiverSet).
                add(receiver);
    }

    /**
     * @todo Use findFirst
     * @todo Post something to note dead letters
     */
    public void post(final Object event) {
        final Class eventType = event.getClass();
        receivers.entrySet().stream().
                filter(e -> e.getKey().isAssignableFrom(eventType)).
                flatMap(e -> e.getValue().stream()).
                forEach(r -> r.accept(event));
    }

    private static Set<Consumer> newReceiverSet(final Class eventType) {
        return new CopyOnWriteArraySet<>();
    }
}
