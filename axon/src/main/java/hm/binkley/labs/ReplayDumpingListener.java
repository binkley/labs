package hm.binkley.labs;

import org.axonframework.eventhandling.replay.ReplayAware;

import static java.lang.System.err;
import static java.lang.System.out;

public class ReplayDumpingListener
        extends DumpingListener
        implements ReplayAware {
    @Override
    public void beforeReplay() {
        out.println("Seems like we're starting a replay");
    }

    @Override
    public void afterReplay() {
        out.println("Seems like we've done replaying");
    }

    @Override
    public void onReplayFailed(final Throwable cause) {
        err.println("The replay failed due to an exception.");
        cause.printStackTrace();
    }
}
