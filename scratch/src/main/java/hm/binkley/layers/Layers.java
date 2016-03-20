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

@RequiredArgsConstructor
public final class Layers<DescriptionType, KeyType>
        extends AbstractList<Layer> {
    private final transient Map<KeyType, Object> cache
            = new LinkedHashMap<>();
    private final DescriptionType description;
    private final Rule<DescriptionType, Object> defaultRule;
    private final List<Layer> layers = new ArrayList<>();
    private final Map<KeyType, Rule> rules = new LinkedHashMap<>();

    public static <DescriptionType> Rule<DescriptionType, Object> defaultRule(
            final DescriptionType description) {
        return new Rule<DescriptionType, Object>(description) {
            @Override
            public Object apply(final Object a, final Object b) {
                return b;
            }
        };
    }

    public DescriptionType description() {
        return description;
    }

    public <T> Optional<T> get(final KeyType key) {
        return Optional.ofNullable((T) cache.get(key));
    }

    public void addRule(final KeyType key, final Rule rule) {
        rules.put(key, rule);
        refresh(key);
    }

    public LayerBuilder<KeyType> layer(final DescriptionType description) {
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
    public static final class LayerBuilder<KeyType> {
        private final Consumer<Map<KeyType, Object>> layers;
        private Map<KeyType, Object> values = new LinkedHashMap<>();

        public LayerBuilder<KeyType> add(final KeyType key,
                final Object value) {
            values.put(key, value);
            return this;
        }

        public void commit() {
            layers.accept(values);
            values = emptyMap();
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    public static final class Layer<DescriptionType, KeyType>
            extends AbstractMap<KeyType, Object> {
        private final DescriptionType description;
        private final Map<KeyType, Object> values;

        public DescriptionType description() {
            return description;
        }

        @Override
        public Set<Entry<KeyType, Object>> entrySet() {
            return unmodifiableMap(values).entrySet();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + description + ", "
                    + values + ")";
        }
    }

    @RequiredArgsConstructor
    @ToString
    public abstract static class Rule<DescriptionType, T>
            implements BinaryOperator<T> {
        private final DescriptionType description;

        public final DescriptionType description() {
            return description;
        }
    }
}
