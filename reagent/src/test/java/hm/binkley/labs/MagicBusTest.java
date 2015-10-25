package hm.binkley.labs;

import hm.binkley.labs.MagicBus.FailedMessage;
import hm.binkley.labs.MagicBus.Mailbox;
import hm.binkley.labs.MagicBus.ReturnedMessage;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicReference;

import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public final class MagicBusTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private AtomicReference<ReturnedMessage> returned;
    private AtomicReference<FailedMessage> failed;
    private MagicBus bus;

    @Before
    public void setUp() {
        returned = new AtomicReference<>();
        failed = new AtomicReference<>();
        bus = new MagicBus(returned::set, failed::set);
    }

    @Test
    public void shouldSubscribeAndPost() {
        final AtomicReference<Foo> mailbox = new AtomicReference<>();
        bus.subscribe(Foo.class, mailbox::set);

        final Bar message = new Bar();
        bus.post(message);

        assertOn(mailbox).
                delivered(message).
                noneReturned().
                noneFailed();
    }

    @Test
    public void shouldUnsubscribe() {
        final AtomicReference<Foo> mailbox = new AtomicReference<>();
        final Mailbox<? super Foo> x = mailbox::set;
        bus.subscribe(Foo.class, x);
        bus.unsubscribe(Foo.class, x);

        final Bar message = new Bar();
        bus.post(message);

        assertOn(noMailbox()).
                noneDelivered().
                returned(message).
                noneFailed();
    }

    @Test
    public void shouldHandleUnsubscribed() {
        final Bar message = new Bar();
        bus.post(message);

        assertOn(noMailbox()).
                noneDelivered().
                returned(message).
                noneFailed();
    }

    @Test
    public void shouldHandleFailed() {
        final Exception e = new TestCheckedException();
        final Mailbox<Foo> mailbox = __ -> {
            throw e;
        };
        bus.subscribe(Foo.class, mailbox);

        final Bar message = new Bar();
        bus.post(message);

        assertOn(noMailbox()).
                noneDelivered().
                noneReturned().
                failed(mailbox, message, e);
    }

    @Test
    public void shouldPassOutRuntimeException() {
        final RuntimeException e = new TestUncheckedException();
        thrown.expect(is(sameInstance(e)));

        bus.subscribe(Foo.class, __ -> {
            throw e;
        });

        bus.post(new Bar());
    }

    private <T> AssertDelivery<T> assertOn(final AtomicReference<T> message) {
        return new AssertDelivery<>(message);
    }

    private static <T> AtomicReference<T> noMailbox() {
        return new AtomicReference<>();
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class AssertDelivery<T> {
        private final AtomicReference<T> delivered;

        private AssertDelivery<T> noneDelivered() {
            assertThat(delivered.get(), is(nullValue()));
            return this;
        }

        private <U extends T> AssertDelivery<T> delivered(final U delivered) {
            assertThat(this.delivered.get(), is(sameInstance(delivered)));
            return this;
        }

        private AssertDelivery<T> noneReturned() {
            assertThat(returned.get(), is(nullValue()));
            return this;
        }

        private AssertDelivery<T> returned(final T message) {
            final ReturnedMessage returned = MagicBusTest.this.returned.get();
            assertThat(returned,
                    is(equalTo(new ReturnedMessage(bus, message))));
            return this;
        }

        private AssertDelivery<T> noneFailed() {
            assertThat(failed.get(), is(nullValue()));
            return this;
        }

        private AssertDelivery<T> failed(final Mailbox mailbox,
                final T message, final Exception failure) {
            final FailedMessage failed = MagicBusTest.this.failed.get();
            assertThat(failed, is(equalTo(
                    new FailedMessage(bus, mailbox, message, failure))));
            return this;
        }
    }

    private abstract static class Foo {}

    private static final class Bar
            extends Foo {}

    private static final class TestCheckedException
            extends Exception {}

    private static final class TestUncheckedException
            extends RuntimeException {}
}
