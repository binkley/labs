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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Collections.emptyMap;
import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

/**
 * @param <DescriptionType> the description type
 * @param <KeyType> the key type
 *
 * @todo {@link #fallbackRule} should be last, ala a stack
 */
@RequiredArgsConstructor
@SuppressWarnings("WeakerAccess")
public final class Layers<DescriptionType, KeyType, ValueType>
        extends AbstractList<Layer>
        implements Described<DescriptionType> {
    private final transient Map<KeyType, ValueType> cache
            = new LinkedHashMap<>();
    private final DescriptionType description;
    private final List<Layer<DescriptionType, KeyType, ValueType>> layers
            = new ArrayList<>();
    private final Map<KeyType, Rule<DescriptionType, ValueType>> specificRules
            = new LinkedHashMap<>();
    private final Map<Predicate<? super KeyType>, Rule<DescriptionType,
            ValueType>>
            genericRules = new LinkedHashMap<>();
    // to reverse?

    public static <DescriptionType, KeyType, ValueType>
    Layers<DescriptionType, KeyType, ValueType> withFallbackRule(
            final DescriptionType layersDescription,
            final DescriptionType defaultRuleDescription) {
        final Layers<DescriptionType, KeyType, ValueType> layers
                = new Layers<>(layersDescription);
        layers.addRule(key -> true, fallbackRule(defaultRuleDescription));
        return layers;
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
            ValueType> fallbackRule(
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
        specificRules.put(key, rule);
        refresh(key);
    }

    public void addRule(final Predicate<? super KeyType> keyMatcher,
            final Rule<DescriptionType, ValueType> rule) {
        genericRules.put(keyMatcher, rule);
        cache.keySet().stream().
                filter(keyMatcher).
                forEach(this::refresh);
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

    private Rule<DescriptionType, ValueType> findRule(final KeyType key) {
        if (specificRules.containsKey(key))
            return specificRules.get(key);
        // TODO: Horrors - reverse map entries in a list
        final List<Entry<Predicate<? super KeyType>, Rule<DescriptionType,
                ValueType>>>
                rules = new ArrayList<>(genericRules.entrySet());
        reverse(rules);
        return rules.stream().
                filter(e -> e.getKey().test(key)).
                findFirst().
                map(Entry::getValue).
                orElseThrow(IllegalStateException::new);
    }

    private void refresh(final KeyType key) {
        final Rule<DescriptionType, ValueType> rule = findRule(key);
        layers.stream().
                filter(layer -> layer.containsKey(key)).
                map(layer -> layer.get(key)).
                reduce(rule).
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
