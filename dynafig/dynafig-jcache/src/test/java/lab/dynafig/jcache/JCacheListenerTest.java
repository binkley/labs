package lab.dynafig.jcache;

import lab.dynafig.Default;
import lab.dynafig.Tracking;
import lab.dynafig.Updating;
import org.junit.Before;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder.SingletonFactory;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.event.CacheEntryListener;

import static javax.cache.Caching.getCachingProvider;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code JCacheListenerTest} tests {@link JCacheListener}.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 */
public class JCacheListenerTest {
    private Updating updating;
    private Tracking tracking;
    private Cache<String, String> cache;

    @Before
    public void setUp() {
        final Default dynafig = new Default();
        updating = dynafig;
        tracking = dynafig;

        final SingletonFactory<CacheEntryListener<? super String, ? super String>>
                listenerFactory = new SingletonFactory<>(
                new JCacheListener(updating));
        final MutableCacheEntryListenerConfiguration<String, String>
                listenerConfiguration
                = new MutableCacheEntryListenerConfiguration<>(
                listenerFactory, null, true, true);
        final MutableConfiguration<String, String> configuration
                = new MutableConfiguration<String, String>().
                setTypes(String.class, String.class).
                addCacheEntryListenerConfiguration(listenerConfiguration);

        cache = getCachingProvider().getCacheManager().
                createCache("test", configuration);
    }

    @Test
    public void should() {
        cache.put("bob", "pretzel");

        assertThat(tracking.track("bob").get().get(), is(equalTo("pretzel")));
    }
}
