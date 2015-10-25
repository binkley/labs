package hm.binkley.labs;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.bus.spec.EventBusSpec;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static reactor.bus.Event.wrap;
import static reactor.bus.selector.Selectors.type;

/**
 * {@code MagicBus} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@RequiredArgsConstructor
public class MagicBus {
    private final EventBus bus = new EventBusSpec().
            synchronousDispatcher().
            uncaughtErrorHandler(MagicBus::rethrowUnchecked).
            get();
    private final Map<Subscription<?>, Registration<Object, reactor.fn.Consumer<? extends Event<?>>>>
            subscriptions = new ConcurrentHashMap<>();

    private final Consumer<? super ReturnedMessage> returned;
    private final Consumer<? super FailedMessage> failed;

    public <T> void subscribe(@Nonnull final Class<T> type,
            @Nonnull final Mailbox<? super T> mailbox) {
        subscriptions.put(Subscription
                        .of(requireNonNull(type), requireNonNull(mailbox)),
                bus.on(type(type), event -> receive(mailbox, event)));
    }

    public <T> void unsubscribe(@Nonnull final Class<T> type,
            @Nonnull final Mailbox<? super T> mailbox) {
        subscriptions.remove(Subscription
                .of(requireNonNull(type), requireNonNull(mailbox))).cancel();
    }

    public void post(@Nonnull final Object message) {
        final Class<?> type = message.getClass();
        if (bus.respondsToKey(type)) {
            bus.notify(type, wrap(message));
            return;
        }

        returned.accept(new ReturnedMessage(this, message));
    }

    @SuppressWarnings("unchecked")
    private <T> void receive(final Mailbox<? super T> mailbox,
            final Event<?> event) {
        final T message = (T) event.getData();
        try {
            mailbox.receive(message);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            failed.accept(new FailedMessage(this, mailbox, message, e));
        }
    }

    private static void rethrowUnchecked(final Throwable t) {
        if (t instanceof RuntimeException)
            throw (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else
            throw new RuntimeException("BUG: Did not handle " + t);
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor(staticName = "of")
    private static final class Subscription<T> {
        private final Class<T> type;
        private final Mailbox<? super T> mailbox;
    }

    @FunctionalInterface
    public interface Mailbox<T> {
        void receive(@Nonnull final T message)
                throws Exception;
    }

    @RequiredArgsConstructor(onConstructor = @__(@Nonnull))
    @EqualsAndHashCode
    @ToString
    public static final class ReturnedMessage {
        @Nonnull
        public final MagicBus bus;
        @Nonnull
        public final Object message;
    }

    @RequiredArgsConstructor(onConstructor = @__(@Nonnull))
    @EqualsAndHashCode
    @ToString
    public static final class FailedMessage {
        @Nonnull
        public final MagicBus bus;
        @Nonnull
        public final Mailbox mailbox;
        @Nonnull
        public final Object message;
        @Nonnull
        public final Exception failure;
    }
}
