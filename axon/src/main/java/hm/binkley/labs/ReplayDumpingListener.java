package hm.binkley.labs;

import org.axonframework.eventhandling.replay.ReplayAware;

/**
 * {@code AnotherThreadPrintingEventListener} <strong>needs
 * documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public class ReplayDumpingListener
        extends DumpingListener
        implements ReplayAware {

    @Override
    public void beforeReplay() {
        System.out.println("Seems like we're starting a replay");
    }

    @Override
    public void afterReplay() {
        System.out.println("Seems like we've done replaying");
    }

    @Override
    public void onReplayFailed(final Throwable cause) {
        System.err.println("The replay failed due to an exception.");
        cause.printStackTrace();
    }
}
