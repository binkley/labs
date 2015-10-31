package hm.binkley.labs;

import reactor.Environment;
import reactor.bus.EventBus;

import java.util.concurrent.CountDownLatch;

import static java.lang.System.out;
import static reactor.bus.selector.Selectors.T;

public final class ReagentMain {
    public static void main(final String... args)
            throws Exception {
        // Force jdeps to fail
        com.alexkasko.unsafe.bytearray.ByteArrayTool.unsafe().isUnsafe();

        try (final Environment __ = Environment.initialize()) {
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
    }

    public abstract static class Foo {
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
