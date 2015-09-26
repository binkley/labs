package lab;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * {@code MagicBusTest} tests {@link MagicBus}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class MagicBusTest {
    private MagicBus bus;

    @Before
    public void setUpFixture() {
        bus = new MagicBus();
    }

    @Test
    public void shouldReceiveCorrectType() {
        final AtomicReference<RightType> mailbox = new AtomicReference<>();
        bus.subscribe(RightType.class, mailbox::set);

        bus.post(new RightType());

        assertThat(mailbox.get(), is(notNullValue()));
    }

    @Test
    public void shouldNotReceiveWrongType() {
        final AtomicReference<LeftType> mailbox = new AtomicReference<>();
        bus.subscribe(LeftType.class, mailbox::set);

        bus.post(new RightType());

        assertThat(mailbox.get(), is(nullValue()));
    }

    @Test
    public void shouldReceiveSubtypes() {
        final AtomicReference<BaseType> mailbox = new AtomicReference<>();
        bus.subscribe(BaseType.class, mailbox::set);

        bus.post(new RightType());

        assertThat(mailbox.get(), is(notNullValue()));
    }

    private abstract static class BaseType {}

    private static final class LeftType
            extends BaseType {}

    private static final class RightType
            extends BaseType {}
}
