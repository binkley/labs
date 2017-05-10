package hm.binkley.layers;

import hm.binkley.layers.Layers.Rule;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class LayersTest {
    private Layers<Tag, Key, Value> layers;

    @Before
    public void setUpLayers() {
        layers = Layers.withFallbackRule(Tag.of("Layers XXX"),
                Tag.of("Key " + "YYY"));
    }

    @Test
    public void shouldApplySpecificRule() {
        layers.addRule(Key.of("A"),
                new Rule<Tag, Value>(Tag.of("Oldest first")) {
                    @Override
                    public Value apply(final Value a, final Value b) {
                        return a;
                    }
                });

        layers.layer(Tag.of("Layer #1")).
                add(Key.of("A"), new Value(3) {}).
                commit();
        layers.layer(Tag.of("Layer #1")).
                add(Key.of("A"), new Value(4) {}).
                commit();

        assertThat(layers.get(Key.of("A"))).
                isEqualTo(Optional.of(new Value(3) {}));
    }

    @Test
    public void shouldApplyGenericRule() {
        layers.addRule(key -> true,
                new Rule<Tag, Value>(Tag.of("Oldest first")) {
                    @Override
                    public Value apply(final Value a, final Value b) {
                        return a;
                    }
                });

        layers.layer(Tag.of("Layer #1")).
                add(Key.of("A"), new Value(3) {}).
                commit();
        layers.layer(Tag.of("Layer #1")).
                add(Key.of("A"), new Value(4) {}).
                commit();

        assertThat(layers.get(Key.of("A"))).
                isEqualTo(Optional.of(new Value(3) {}));
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor(staticName = "of")
    private static final class Tag {
        private final String label;

        @Override
        public String toString() {
            return label;
        }
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor(staticName = "of")
    private static final class Key {
        private final String name;

        @Override
        public String toString() {
            return name;
        }
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    private abstract static class Value {
        private final Object value;

        @Override
        public final String toString() {
            return String.valueOf(value);
        }
    }
}
