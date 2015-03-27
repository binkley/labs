package lab.dynafig.archaius;

import com.netflix.archaius.Config;
import com.netflix.archaius.DynamicConfig;
import com.netflix.archaius.DynamicConfigObserver;
import com.netflix.archaius.config.AbstractDynamicConfig;
import lab.dynafig.Updating;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Iterator;

/**
 * {@code ArchaiusListener} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public final class ArchaiusListener
        implements DynamicConfigObserver {
    private final Updating updating;

    @Inject
    public ArchaiusListener(@Nonnull final Updating updating,
            @Nonnull final DynamicConfig config) {
        this.updating = updating;
        config.addListener(this);
        onUpdate(config);
    }

    @Override
    public void onUpdate(final String propName, final Config config) {
        updating.update(propName, config.getString(propName));
    }

    @Override
    public void onUpdate(final Config config) {
        final Iterator<String> keys = config.getKeys();
        while (keys.hasNext())
            onUpdate(keys.next(), config);
    }

    @Override
    public void onError(final Throwable error, final Config config) {

    }

    public static final class PassThroughDynamicConfig extends
            AbstractDynamicConfig {
        public PassThroughDynamicConfig(final String name) {
            super(name);
        }

        @Override
        public String getRawString(final String key) {
            return null;
        }

        @Override
        public boolean containsProperty(final String key) {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Iterator<String> getKeys() {
            return null;
        }
    }
}
