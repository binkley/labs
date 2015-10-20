package hm.binkley.labs;

import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.eventstore.EventStore;
import org.axonframework.eventstore.EventVisitor;
import org.axonframework.eventstore.management.Criteria;
import org.axonframework.eventstore.management.CriteriaBuilder;
import org.axonframework.eventstore.management.EventStoreManagement;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@code StubEventStore} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public class StubEventStore
        implements EventStoreManagement, EventStore {

    private final List<DomainEventMessage> eventMessages
            = new CopyOnWriteArrayList<>();

    @Override
    public void appendEvents(final String type, final DomainEventStream events) {
        while (events.hasNext()) {
            eventMessages.add(events.next());
        }
    }

    @Override
    public void visitEvents(final EventVisitor visitor) {
        eventMessages.forEach(visitor::doWithEvent);
    }

    @Override
    public void visitEvents(final Criteria criteria, final EventVisitor visitor) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CriteriaBuilder newCriteriaBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public DomainEventStream readEvents(final String type, final Object identifier) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
