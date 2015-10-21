package hm.binkley.labs;

import hm.binkley.labs.MagicBus.FailedMessage;
import hm.binkley.labs.MagicBus.UnsubscribedMessage;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public final class MagicBusTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private List<UnsubscribedMessage> returned;
    private List<FailedMessage> failed;
    private MagicBus bus;

    @Before
    public void setUp() {
        returned = new ArrayList<>();
        failed = new ArrayList<>();
        bus = new MagicBus(returned::add, failed::add);
    }

    @Test
    public void shouldSubscribeAndPost() {
        final List<Foo> messages = new ArrayList<>();
        bus.subscribe(Foo.class, messages::add);

        final Bar message = new Bar();
        bus.post(message);

        assertOn(messages).
                delivered(1).
                returned(0).
                failed(0);
        assertThat(messages.get(0), is(sameInstance(message)));
    }

    @Test
    public void shouldHandleUnsubscribed() {
        final Bar message = new Bar();
        bus.post(message);

        assertOn(emptyList()).
                delivered(0).
                returned(1).
                failed(0);
        final UnsubscribedMessage returned = this.returned.get(0);
        assertThat(returned.message, is(sameInstance(message)));
    }

    @Test
    public void shouldHandleFailed() {
        final Exception e = new TestCheckedException();
        bus.subscribe(Foo.class, __ -> {
            throw e;
        });

        final Bar message = new Bar();
        bus.post(message);

        assertOn(emptyList()).
                delivered(0).
                returned(0).
                failed(1);
        final FailedMessage failed = this.failed.get(0);
        assertThat(failed.message, is(sameInstance(message)));
        assertThat(failed.failure, is(sameInstance(e)));
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

    private AssertDelivery assertOn(final List messages) {
        return new AssertDelivery(messages);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class AssertDelivery {
        private final List<?> messages;

        private AssertDelivery delivered(final int delivered) {
            assertThat(messages, hasSize(delivered));
            return this;
        }

        private AssertDelivery returned(final int returned) {
            assertThat(MagicBusTest.this.returned, hasSize(returned));
            return this;
        }

        private AssertDelivery failed(final int failed) {
            assertThat(MagicBusTest.this.failed, hasSize(failed));
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
