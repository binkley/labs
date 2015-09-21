package lab.dynafig.jcache;

import lab.dynafig.Updating;

import javax.annotation.Nonnull;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.inject.Inject;

/**
 * {@code JCacheListener} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley</a>
 * @todo Needs documentation
 */
public final class JCacheListener
        implements CacheEntryCreatedListener<String, String>,
        CacheEntryUpdatedListener<String, String> {
    private final Updating updating;

    @Inject
    public JCacheListener(@Nonnull final Updating updating) {
        this.updating = updating;
    }

    @Override
    public void onCreated(
            final Iterable<CacheEntryEvent<? extends String, ? extends String>> events)
            throws CacheEntryListenerException {
        for (final CacheEntryEvent<? extends String, ? extends String> event : events)
            updating.update(event.getKey(), event.getValue());
    }

    @Override
    public void onUpdated(
            final Iterable<CacheEntryEvent<? extends String, ? extends String>> events)
            throws CacheEntryListenerException {
        for (final CacheEntryEvent<? extends String, ? extends String> event : events)
            updating.update(event.getKey(), event.getValue());
    }
}
