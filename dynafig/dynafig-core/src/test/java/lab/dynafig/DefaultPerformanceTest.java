package lab.dynafig;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Runtime.getRuntime;
import static java.util.Collections.singletonMap;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * {@code DefaultPerformanceTest} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public class DefaultPerformanceTest {
    private final Default dynafig = new Default(
            singletonMap("bob", randomUUID().toString()));
    private final AtomicReference<String> bob = dynafig.track("bob").get();
    private final String oldValue = bob.get();

    @Ignore("What is right thing to test?")
    @Test(timeout = 10000L)
    public void shouldBeConsistent()
            throws InterruptedException {
        // TODO: Is common pool right here?
        final int cores = 2 * getRuntime().availableProcessors();
        final ExecutorService pool = newFixedThreadPool(cores);
        final List<Future<String>> tests = pool.
                invokeAll(range(0, cores).
                        mapToObj(this::runCheck).
                        collect(toList()));
        SECONDS.sleep(2);
        pool.shutdownNow();
        for (final Future<String> dead : tests)
            try {
                dead.get();
            } catch (final ExecutionException e) {
                final Throwable x = e.getCause();
                if (x instanceof Error)
                    throw (Error) x;
                else if (x instanceof RuntimeException)
                    throw (RuntimeException) x;
                else
                    throw new IllegalStateException(x);
            }
        assertThat(bob.get(), is(not(oldValue)));
    }

    private Callable<String> runCheck(final int n) {
        return () -> {
            final String newValue = randomUUID().toString();
            dynafig.update("bob", newValue);
            MILLISECONDS.sleep(10);
            assertThat(bob.get(),
                    is(not(newValue))); // TODO: Bad test on purpose
            return null;
        };
    }
}
