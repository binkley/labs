package hm.binkley.labs;

import org.jctools.queues.ConcurrentCircularArrayQueue;
import org.jctools.queues.MpmcArrayQueue;
import org.jctools.queues.MpscArrayQueue;

/**
 * {@code JCToolsMain} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public final class JCToolsMain {
    public static void main(final String... args) {
        final ConcurrentCircularArrayQueue<Foo> q1 = new MpscArrayQueue<>(10);
        final ConcurrentCircularArrayQueue<Foo> q2 = new MpmcArrayQueue<>(10);

        q1.relaxedOffer(new Bar());
        System.out.println(q1.relaxedPoll());
    }

    public static abstract class Foo {
    }

    public static final class Bar
            extends Foo {
    }
}
