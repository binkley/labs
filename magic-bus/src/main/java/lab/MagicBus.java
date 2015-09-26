package lab;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * {@code MagicBus} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@RequiredArgsConstructor
public final class MagicBus {
    public interface Receiver<T> {
        void receive(final T message)
                throws Exception;
    }

    private final Subscribers subscribers = new Subscribers();
    private final Consumer<DeadLetter> returned;
    private final Consumer<FailedPost> failed;

    public <T> void subscribe(final Class<T> messageType,
            final Receiver<? super T> receiver) {
        subscribers.subscribe(messageType, receiver);
    }

    public void post(final Object message) {
        final boolean[] found = {false};
        subscribers.of(message).
                peek(r -> found[0] = true).
                forEach(receive(message));
        if (!found[0])
            returned.accept(new DeadLetter(this, message));
    }

    @SuppressWarnings("unchecked")
    private Consumer<Receiver> receive(final Object message) {
        return receiver -> {
            try {
                receiver.receive(message);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                failed.accept(new FailedPost(this, receiver, message, e));
            }
        };
    }

    @RequiredArgsConstructor
    public static final class DeadLetter {
        public final MagicBus bus;
        public final Object message;
    }

    @RequiredArgsConstructor
    public static final class FailedPost
            extends RuntimeException {
        public final MagicBus bus;
        public final Receiver receiver;
        public final Object message;
        public final Exception failure;

        private void doom(final Exception e) {
            failure.addSuppressed(e);
            throw this;
        }
    }

    private static final class Subscribers {
        private final Map<Class, Set<Receiver>> subscribers
                = new ConcurrentHashMap<>();

        private void subscribe(final Class messageType,
                final Receiver receiver) {
            subscribers.computeIfAbsent(messageType, Subscribers::receivers).
                    add(receiver);
        }

        private Stream<Receiver> of(final Object message) {
            final Class messageType = message.getClass();
            return subscribers.entrySet().stream().
                    filter(subscribedTo(messageType)).
                    flatMap(toReceivers());
        }

        private static Set<Receiver> receivers(final Class messageType) {
            return new CopyOnWriteArraySet<>();
        }

        @SuppressWarnings("unchecked")
        private static Predicate<Entry<Class, Set<Receiver>>> subscribedTo(
                final Class messageType) {
            return e -> e.getKey().isAssignableFrom(messageType);
        }

        private static Function<Entry<Class, Set<Receiver>>, Stream<? extends Receiver>> toReceivers() {
            return e -> e.getValue().stream();
        }
    }
}
