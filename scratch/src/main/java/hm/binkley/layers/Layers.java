package hm.binkley.layers;

import hm.binkley.layers.Layers.Layer;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

/**
 * @param <DescriptionType> the description type
 * @param <KeyType> the key type
 *
 * @todo {@link #defaultRule} should be last, ala a stack
 */
@RequiredArgsConstructor
@SuppressWarnings("WeakerAccess")
public final class Layers<DescriptionType, KeyType, ValueType>
        extends AbstractList<Layer>
        implements Described<DescriptionType> {
    private final transient Map<KeyType, ValueType> cache
            = new LinkedHashMap<>();
    private final DescriptionType description;
    private final Rule<DescriptionType, ValueType> defaultRule;
    private final List<Layer<DescriptionType, KeyType, ValueType>> layers
            = new ArrayList<>();
    private final Map<KeyType, Rule<DescriptionType, ValueType>> rules
            = new LinkedHashMap<>();

    public static Layers<String, String, Object> vanilla(
            final String layersDescription,
            final String defaultRuleDescription) {
        return new Layers<>(layersDescription,
                defaultRule(defaultRuleDescription));
    }

    /**
     * Constructs a default {@link Rule} for the given <var>description</var>.
     * The default rule treats layers as a stack, returning the key value of
     * the most recent (topmost) layer containing that key.
     *
     * @param description the default rule description, never missing
     * @param <DescriptionType> the description type
     * @param <ValueType> the value type
     *
     * @return the default rule for {@code DescriptionType}, never missing
     */
    public static <DescriptionType, ValueType> Rule<DescriptionType,
            ValueType> defaultRule(
            final DescriptionType description) {
        return new Rule<DescriptionType, ValueType>(description) {
            @Override
            public ValueType apply(final ValueType a, final ValueType b) {
                return b;
            }
        };
    }

    @Override
    public DescriptionType description() {
        return description;
    }

    @SuppressWarnings("unchecked")
    public <T extends ValueType> Optional<T> get(final KeyType key) {
        return Optional.ofNullable((T) cache.get(key));
    }

    public void addRule(final KeyType key,
            final Rule<DescriptionType, ValueType> rule) {
        rules.put(key, rule);
        refresh(key);
    }

    public LayerBuilder<KeyType, ValueType> layer(
            final DescriptionType description) {
        return new LayerBuilder<>(values -> {
            layers.add(new Layer<>(description, values));
            values.keySet().forEach(this::refresh);
        });
    }

    @Override
    public int size() {
        return layers.size();
    }

    @Override
    public Layer get(final int index) {
        return layers.get(index);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + description + ", " +
                layers.stream().map(Layer::description).collect(toList())
                + ", " + cache + ")";
    }

    private void refresh(final KeyType key) {
        layers.stream().
                filter(layer -> layer.containsKey(key)).
                map(layer -> layer.get(key)).
                reduce(rules.computeIfAbsent(key, k -> defaultRule)).
                ifPresent(value -> cache.put(key, value));
    }

    @RequiredArgsConstructor(access = PRIVATE)
    @SuppressWarnings("WeakerAccess")
    public static final class LayerBuilder<KeyType, ValueType> {
        private final Consumer<Map<KeyType, ValueType>> layers;
        private Map<KeyType, ValueType> values = new LinkedHashMap<>();

        public LayerBuilder<KeyType, ValueType> add(final KeyType key,
                final ValueType value) {
            values.put(key, value);
            return this;
        }

        public void commit() {
            layers.accept(values);
            values = emptyMap();
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    @SuppressWarnings(
            {"MismatchedQueryAndUpdateOfCollection", "WeakerAccess"})
    public static final class Layer<DescriptionType, KeyType, ValueType>
            extends AbstractMap<KeyType, ValueType>
            implements Described<DescriptionType> {
        private final DescriptionType description;
        private final Map<KeyType, ValueType> values;

        @Override
        public DescriptionType description() {
            return description;
        }

        @Override
        public Set<Entry<KeyType, ValueType>> entrySet() {
            return unmodifiableMap(values).entrySet();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + description + ", "
                    + values + ")";
        }
    }

    @RequiredArgsConstructor(access = PROTECTED)
    @ToString
    public abstract static class Rule<DescriptionType, ValueType>
            implements BinaryOperator<ValueType>, Described<DescriptionType> {
        private final DescriptionType description;

        @Override
        public final DescriptionType description() {
            return description;
        }
    }
}
