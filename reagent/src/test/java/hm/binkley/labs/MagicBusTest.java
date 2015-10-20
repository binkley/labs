package hm.binkley.labs;

import hm.binkley.labs.MagicBus.FailedMessage;
import hm.binkley.labs.MagicBus.UnsubscribedMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * {@code MagicBusTest} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public class MagicBusTest {
    private AtomicReference<UnsubscribedMessage> unsubscribed;
    private AtomicReference<FailedMessage> failed;
    private MagicBus bus;

    @Before
    public void setUp() {
        unsubscribed = new AtomicReference<>();
        failed = new AtomicReference<>();
        bus = new MagicBus(unsubscribed::set, failed::set);
    }

    @Test
    public void shouldSubscribeAndPost() {
        final AtomicReference<Foo> mailbox = new AtomicReference<>();
        bus.subscribe(Foo.class, mailbox::set);

        final Bar message = new Bar();
        bus.post(message);

        assertThat(mailbox.get(), is(sameInstance(message)));
        assertThat(unsubscribed.get(), is(nullValue()));
        assertThat(failed.get(), is(nullValue()));
    }

    @Test
    public void shouldHandleUnsubscribed() {
        final Bar message = new Bar();
        bus.post(message);
        final UnsubscribedMessage unsubscribed = this.unsubscribed.get();

        assertThat(unsubscribed.message, is(sameInstance(message)));
    }

    @Test
    public void shouldHandleFailed() {
        bus.subscribe(Foo.class, __ -> {
            throw new Exception("Foo!");
        });

        final Bar message = new Bar();
        bus.post(message);
        final FailedMessage failed = this.failed.get();

        assertThat(failed.message, is(sameInstance(message)));
    }

    private static abstract class Foo {
    }

    private static final class Bar
            extends Foo {
    }
}
