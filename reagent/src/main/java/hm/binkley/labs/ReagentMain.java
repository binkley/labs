package hm.binkley.labs;

import reactor.Environment;
import reactor.bus.EventBus;

import java.util.concurrent.CountDownLatch;

import static java.lang.System.out;
import static reactor.bus.selector.Selectors.T;

/**
 * {@code ReagentMain} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 * @see <a href="http://projectreactor.io/docs/reference/#reactor-bus">reactor-bus</a>
 */
public final class ReagentMain {
    static {
        Environment.initialize();
    }

    public static void main(final String... args)
            throws InterruptedException {
        // Because main() exits before bus can process, force it to wait
        final CountDownLatch done = new CountDownLatch(1);
        final EventBus bus = EventBus.create(Environment.get());

        bus.on(T(Foo.class), ev -> {
            out.println(ev);
            done.countDown();
        });

        bus.notify(new Bar());

        done.await();
    }

    public static abstract class Foo {
        @Override
        public String toString() {
            return "I'm a Foo!";
        }
    }

    public static final class Bar
            extends Foo {
        @Override
        public String toString() {
            return "I'm a Bar!";
        }
    }
}
