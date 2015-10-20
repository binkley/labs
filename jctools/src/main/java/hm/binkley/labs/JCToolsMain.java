package hm.binkley.labs;

import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpmcArrayQueue;
import org.jctools.queues.MpscArrayQueue;

import static java.lang.System.out;
import static java.util.Arrays.asList;

/**
 * {@code JCToolsMain} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public final class JCToolsMain {
    public static void main(final String... args) {
        final MessagePassingQueue<Foo> q1 = new MpscArrayQueue<>(2);
        final MessagePassingQueue<Foo> q2 = new MpmcArrayQueue<>(2);

        for (final MessagePassingQueue<Foo> q : asList(q1, q2)) {
            q.relaxedOffer(new Bar());
            out.println(q.relaxedPoll());
        }
    }

    public static abstract class Foo {
    }

    public static final class Bar
            extends Foo {
    }
}
