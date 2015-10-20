package hm.binkley.labs;

import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.GenericDomainEventMessage;
import org.axonframework.domain.SimpleDomainEventStream;
import org.axonframework.eventhandling.ClusteringEventBus;
import org.axonframework.eventhandling.DefaultClusterSelector;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleCluster;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;
import org.axonframework.eventhandling.replay.BackloggingIncomingMessageHandler;
import org.axonframework.eventhandling.replay.ReplayingCluster;
import org.axonframework.unitofwork.NoTransactionManager;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.axonframework.domain.GenericEventMessage.asEventMessage;

/**
 * {@code AxonMain} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public final class AxonMain {
    public static void main(final String[] args)
            throws InterruptedException, TimeoutException,
            ExecutionException {

        // we initialize an event store that is capable of doing replays. In this case, we create a Stub implementation.
        // In production, you would use an Event Store implementation such as the JpaEventStore or MongoEventStore.
        final StubEventStore eventStore = new StubEventStore();

        //we create a ReplayingCluster, which wraps that actual cluster that listeners will be subscribed to
        // since we don't need transactions in this in-memory sample, we use a NoTransactionManager
        // the 0 means we do not need any "intermediate commit" during the replay.
        // The BackloggingIncomingMessageHandler will make sure any events published while replaying are backlogged
        // and postponed until the replay is done.
        final ReplayingCluster replayingCluster = new ReplayingCluster(
                new SimpleCluster("simple"), eventStore,
                new NoTransactionManager(), 0,
                new BackloggingIncomingMessageHandler());

        // we initialize an event bus that contains our replaying cluster
        final EventBus eventBus = new ClusteringEventBus(
                new DefaultClusterSelector(replayingCluster));

        // we subscribe our two listeners to the Event Bus
        AnnotationEventListenerAdapter
                .subscribe(new DumpingListener(), eventBus);
        AnnotationEventListenerAdapter
                .subscribe(new ReplayDumpingListener(), eventBus);

        // we append some events to simulate a full event store
        final DomainEventMessage[] domainEventMessages = {
                new GenericDomainEventMessage<>("todo1", 0,
                        new ToDoItemCreatedEvent("todo1",
                                "Need to do something")),
                new GenericDomainEventMessage<>("todo2", 0,
                        new ToDoItemCreatedEvent("todo2",
                                "Another thing to do")),
                new GenericDomainEventMessage<>("todo2", 0,
                        new ToDoItemCompletedEvent("todo2"))};
        eventStore.appendEvents("mock",
                new SimpleDomainEventStream(domainEventMessages));

        // we create an executor service with a single thread and start the replay as an asynchronous process
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<Void> future = replayingCluster.startReplay(executor);

        // we want to wait for the cluster to have switched to replay mode, so we can send some messages to it.
        // if we were to publish events right away, there is a big chance the Cluster didn't switch to replay mode, yet.
        waitForReplayToHaveStarted(replayingCluster);

        // this is a new event, so it should be backlogged and handled at the end of the replay
        eventBus.publish(asEventMessage(
                new ToDoItemCreatedEvent("todo3", "Came in just now...")));

        // this message is also part of the replay, and should therefore not be handled twice.
        eventBus.publish(domainEventMessages[2]);

        // we wait (at most 10 seconds) for the replay to complete.
        future.get(10, TimeUnit.SECONDS);

        // and we publish another event to show that it's handled in the calling thread
        eventBus.publish(
                asEventMessage(new ToDoItemDeadlineExpiredEvent("todo1")));

        // we want to shutdown the executor, to get a proper JVM shutdown
        executor.shutdown();
    }

    private static void waitForReplayToHaveStarted(
            final ReplayingCluster replayingCluster) {
        while (!replayingCluster.isInReplayMode()) {
            Thread.yield();
        }
    }
}
