package hm.binkley.labs;

import javax.annotation.Nonnull;
import java.io.IOError;
import java.io.IOException;

/**
 * {@code IORunnable} is a {@link Runnable} which throws {@link IOException}.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @see #rethrow(IORunnable) Wrap to a plain runnable
 */
@FunctionalInterface
public interface IORunnable {
    /**
     * Wraps an I/O <var>runnable</var> to rethrow {@link IOException} as
     * {@link IOError}.
     *
     * @param runnable the runnable, never {@code null}
     *
     * @return the wrapped runnable, never {@code null}
     */
    @Nonnull
    static Runnable rethrow(@Nonnull final IORunnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (final IOException e) {
                throw new IOError(e);
            }
        };
    }

    /** @see {@link Runnable#run()} */
    void run()
            throws IOException;
}
