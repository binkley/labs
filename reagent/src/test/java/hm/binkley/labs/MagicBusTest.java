package hm.binkley.labs;

import hm.binkley.labs.MagicBus.FailedMessage;
import hm.binkley.labs.MagicBus.Mailbox;
import hm.binkley.labs.MagicBus.ReturnedMessage;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public final class MagicBusTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private List<ReturnedMessage> returned;
    private List<FailedMessage> failed;
    private MagicBus bus;

    @Before
    public void setUp() {
        returned = new CopyOnWriteArrayList<>();
        failed = new CopyOnWriteArrayList<>();
        bus = new MagicBus(returned::add, failed::add);
    }

    @Test
    public void shouldSubscribeAndPost() {
        final List<Foo> mailbox = new CopyOnWriteArrayList<>();
        bus.subscribe(Foo.class, mailbox::add);

        final Bar message = new Bar();
        bus.post(message);

        assertOn(mailbox).
                delivered(message).
                noneReturned().
                noneFailed();
    }

    @Test
    public void shouldSubscribeAndPostToDifferentMailboxes() {
        final List<Foo> mailboxA = new CopyOnWriteArrayList<>();
        bus.subscribe(Foo.class, mailboxA::add);
        final List<Foo> mailboxB = new CopyOnWriteArrayList<>();
        bus.subscribe(Foo.class, mailboxB::add);

        final Bar message = new Bar();
        bus.post(message);

        assertOn(mailboxA).
                delivered(message).
                noneReturned().
                noneFailed();
        assertOn(mailboxB).
                delivered(message).
                noneReturned().
                noneFailed();
    }

    @Test
    public void shouldSubscribeAndPostToSameMailboxForDifferentTypes() {
        final List<Foo> mailboxA = new CopyOnWriteArrayList<>();
        bus.subscribe(Foo.class, mailboxA::add);
        final List<Bar> mailboxB = new CopyOnWriteArrayList<>();
        bus.subscribe(Bar.class, mailboxB::add);

        final Bar message = new Bar();
        bus.post(message);

        assertOn(mailboxA).
                delivered(message).
                noneReturned().
                noneFailed();
        assertOn(mailboxB).
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
                returned(with(message)).
                noneFailed();
    }

    @Test
    public void shouldHandleUnsubscribed() {
        final Bar message = new Bar();
        bus.post(message);

        assertOn(noMailbox()).
                noneDelivered().
                returned(with(message)).
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
                failed(with(mailbox, message, e));
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

    private <T> AssertDelivery<T> assertOn(final List<T> delivered) {
        return new AssertDelivery<>(delivered);
    }

    private static <T> List<T> noMailbox() {
        return emptyList();
    }

    private ReturnedMessage with(final Object message) {
        return new ReturnedMessage(bus, message);
    }

    private FailedMessage with(final Mailbox mailbox, final Object message,
            final Exception failure) {
        return new FailedMessage(bus, mailbox, message, failure);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class AssertDelivery<T> {
        private final List<T> delivered;

        private AssertDelivery<T> noneDelivered() {
            assertThat(delivered, is(empty()));
            return this;
        }

        @SafeVarargs
        private final <U extends T> AssertDelivery<T> delivered(
                final U... delivered) {
            assertThat(this.delivered, is(asList(delivered)));
            return this;
        }

        private AssertDelivery<T> noneReturned() {
            assertThat(returned, is(emptyList()));
            return this;
        }

        private AssertDelivery<T> returned(
                final ReturnedMessage... returned) {
            assertThat(MagicBusTest.this.returned,
                    is(equalTo(asList(returned))));
            return this;
        }

        private AssertDelivery<T> noneFailed() {
            assertThat(failed, is(emptyList()));
            return this;
        }

        private AssertDelivery<T> failed(final FailedMessage... failed) {
            assertThat(MagicBusTest.this.failed, is(equalTo(asList(failed))));
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
