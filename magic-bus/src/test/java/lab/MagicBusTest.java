package lab;

import lab.MagicBus.FailedPost;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

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

    @Test
    public void shouldReceiveFailedPosts() {
        final AtomicReference<FailedPost> mailbox = new AtomicReference<>();
        bus.subscribe(FailedPost.class, mailbox::set);
        bus.subscribe(LeftType.class, message -> {
            throw new Exception();
        });

        bus.post(new LeftType());

        assertThat(mailbox.get(), is(notNullValue()));
    }

    @Test
    public void shouldNotRecurWhenFailedPostFails() {
        thrown.expect(FailedPost.class);

        bus.subscribe(FailedPost.class, failedPost -> {
            throw new Exception();
        });
        bus.subscribe(LeftType.class, message -> {
            throw new Exception();
        });

        bus.post(new LeftType());
    }

    private abstract static class BaseType {}

    private static final class LeftType
            extends BaseType {}

    private static final class RightType
            extends BaseType {}
}
