package lab.dynafig.jcache;

import lab.dynafig.Default;
import lab.dynafig.Tracking;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.FactoryBuilder.SingletonFactory;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.event.CacheEntryListener;
import javax.cache.spi.CachingProvider;

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
    private Tracking tracking;

    private CachingProvider cachingProvider;
    private CacheManager cacheManager;
    private Cache<String, String> cache;

    @Before
    public void setUp() {
        final Default dynafig = new Default();
        tracking = dynafig;

        final SingletonFactory<CacheEntryListener<? super String, ? super String>>
                listenerFactory = new SingletonFactory<>(
                new JCacheListener(dynafig));
        final MutableCacheEntryListenerConfiguration<String, String>
                listenerConfiguration
                = new MutableCacheEntryListenerConfiguration<>(
                listenerFactory, null, true, true);
        final MutableConfiguration<String, String> configuration
                = new MutableConfiguration<String, String>().
                setTypes(String.class, String.class).
                addCacheEntryListenerConfiguration(listenerConfiguration);

        cachingProvider = getCachingProvider();
        cacheManager = cachingProvider.getCacheManager();
        cache = cacheManager.createCache("test", configuration);
    }

    @Test
    public void shouldTrackUpdate() {
        cache.put("bob", "pretzel");

        assertThat(tracking.track("bob").get().get(), is(equalTo("pretzel")));
    }

    @After
    public void tearDown() {
        cache.close();
        cacheManager.close();
        cachingProvider.close();
    }
}
