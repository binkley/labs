package hm.binkley.labs;

import org.junit.Before;
import org.junit.Test;
import reactor.bus.Event;
import reactor.bus.EventBus;

import java.util.concurrent.atomic.AtomicReference;

import static hm.binkley.labs.EventDataMatcher.hasEventData;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static reactor.bus.selector.Selectors.$;

/**
 * {@code ReagentMain} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 * @see <a href="http://projectreactor.io/docs/reference/#reactor-bus">reactor-bus</a>
 */
public final class ReagentTest {
    private EventBus bus;

    @Before
    public void setUpFixture() {
        bus = EventBus.create();
    }

    @Test
    public void should()
            throws InterruptedException {
        final AtomicReference<Event<String>> inbox = new AtomicReference<>();
        bus.on($("topic"), inbox::set);

        final String data = "foo!";
        bus.notify("topic", Event.wrap(data));

        assertThat(inbox.get(), hasEventData(equalTo(data)));
    }
}
