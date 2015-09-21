package lab.dynafig.jcache;

import lab.dynafig.DefaultDynafig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.cache.Cache;
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
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley</a>
 */
public class JCacheListenerTest {
    private static CachingProvider cachingProvider;

    private DefaultDynafig dynafig;
    private Cache<String, String> cache;

    @BeforeClass
    public static void setUpCachingProvider() {
        cachingProvider = getCachingProvider();
    }

    @Before
    public void setUp() {
        dynafig = new DefaultDynafig();

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

        cache = cachingProvider.
                getCacheManager().
                createCache("test", configuration);
    }

    @Test
    public void shouldTrackUpdate() {
        cache.put("bob", "pretzel");

        assertThat(dynafig.track("bob").get().get(), is(equalTo("pretzel")));
    }

    @AfterClass
    public static void tearDownCaching() {
        cachingProvider.close();
    }
}
