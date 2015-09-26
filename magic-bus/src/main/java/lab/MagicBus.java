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
public final class MagicBus {
    public interface Receiver<T> {
        void receive(final T message)
                throws Exception;
    }

    private final Receivers receivers = new Receivers();

    public <T> void subscribe(final Class<T> messageType,
            final Receiver<? super T> receiver) {
        receivers.subscribe(messageType, receiver);
    }

    public void post(final Object message) {
        receivers.subscribedTo(message).
                forEach(receive(message));
    }

    @SuppressWarnings("unchecked")
    private Consumer<Receiver> receive(final Object message) {
        return receiver -> {
            try {
                receiver.receive(message);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                if (message instanceof FailedPost)
                    ((FailedPost) message).doom(e);
                post(new FailedPost(this, receiver, message, e));
            }
        };
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

    private static final class Receivers {
        private final Map<Class, Set<Receiver>> receivers
                = new ConcurrentHashMap<>();

        private void subscribe(final Class messageType,
                final Receiver receiver) {
            receivers.computeIfAbsent(messageType, Receivers::newReceiverSet).
                    add(receiver);
        }

        private Stream<Receiver> subscribedTo(final Object message) {
            final Class messageType = message.getClass();
            return receivers.entrySet().stream().
                    filter(subscribedTo(messageType)).
                    flatMap(toReceivers());
        }

        private static Set<Receiver> newReceiverSet(final Class messageType) {
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
