package hm.binkley.labs;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import reactor.bus.Event;

/**
 * {@code EventDataMatcher} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public final class EventDataMatcher<T>
        extends TypeSafeMatcher<Event<? super T>> {
    private final Matcher<? super T> dataMatcher;

    public static <T> EventDataMatcher<T> hasEventData(
            final Matcher<? super T> dataMatcher) {
        return new EventDataMatcher<>(dataMatcher);
    }

    public EventDataMatcher(final Matcher<? super T> dataMatcher) {
        this.dataMatcher = dataMatcher;
    }

    @Override
    protected boolean matchesSafely(final Event<? super T> item) {
        return dataMatcher.matches(item.getData());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an event with data matching ");
        dataMatcher.describeTo(description);
    }
}
