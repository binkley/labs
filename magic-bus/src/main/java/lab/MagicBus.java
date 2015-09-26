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

import static java.util.stream.StreamSupport.stream;
import static lab.CountedSpliterator.count;

/**
 * {@code MagicBus} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@RequiredArgsConstructor
public final class MagicBus {
    private final Subscribers subscribers = new Subscribers();
    private final Consumer<DeadLetter> returned;
    private final Consumer<FailedPost> failed;

    public <T> void subscribe(final Class<T> messageType,
            final Mailbox<? super T> mailbox) {
        subscribers.subscribe(messageType, mailbox);
    }

    public void post(final Object message) {
        final CountedSpliterator<Mailbox> it = count(subscribers.of(message));
        stream(it, false).
                forEach(receive(message));

        if (it.isEmpty())
            returned.accept(new DeadLetter(this, message));
    }

    @SuppressWarnings("unchecked")
    private Consumer<Mailbox> receive(final Object message) {
        return mailbox -> {
            try {
                mailbox.receive(message);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                failed.accept(new FailedPost(this, mailbox, message, e));
            }
        };
    }

    @FunctionalInterface
    public interface Mailbox<T> {
        void receive(final T message)
                throws Exception;
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
        public final Mailbox mailbox;
        public final Object message;
        public final Exception failure;
    }

    private static final class Subscribers {
        private final Map<Class, Set<Mailbox>> subscribers
                = new ConcurrentHashMap<>();

        private void subscribe(final Class messageType,
                final Mailbox mailbox) {
            subscribers.computeIfAbsent(messageType, Subscribers::mailbox).
                    add(mailbox);
        }

        private Stream<Mailbox> of(final Object message) {
            final Class messageType = message.getClass();
            return subscribers.entrySet().stream().
                    filter(subscribedTo(messageType)).
                    flatMap(toMailboxes());
        }

        private static Set<Mailbox> mailbox(final Class messageType) {
            return new CopyOnWriteArraySet<>();
        }

        @SuppressWarnings("unchecked")
        private static Predicate<Entry<Class, Set<Mailbox>>> subscribedTo(
                final Class messageType) {
            return e -> e.getKey().isAssignableFrom(messageType);
        }

        private static Function<Entry<Class, Set<Mailbox>>, Stream<Mailbox>> toMailboxes() {
            return e -> e.getValue().stream();
        }
    }
}
