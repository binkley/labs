package hm.binkley.labs;

import hm.binkley.labs.Layers.Layer;
import hm.binkley.labs.LayersMain.Tag;
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
public final class Layers<DescriptionType>
        extends AbstractList<Layer> {
    private final transient Map<String, Optional> cache
            = new LinkedHashMap<>();
    private final DescriptionType description;
    private final List<Layer> layers = new ArrayList<>();
    private final Map<String, Rule> rules = new LinkedHashMap<>();

    public DescriptionType description() {
        return description;
    }

    public <T> Optional<T> get(final String key) {
        return cache.getOrDefault(key, Optional.empty());
    }

    public void addRule(final String key, final Rule rule) {
        rules.put(key, rule);
        refresh(key);
    }

    public LayerBuilder layer(final DescriptionType description) {
        return new LayerBuilder(values -> {
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

    private void refresh(final String key) {
        cache.put(key, layers.stream().
                filter(layer -> layer.containsKey(key)).
                map(layer -> layer.get(key)).
                reduce(rules.computeIfAbsent(key, k -> new Rule<Tag, Object>(
                        Tag.of("Default rule - take last", 0)) {
                    @Override
                    public Object apply(final Object o, final Object o2) {
                        return o2;
                    }
                })));
    }

    @RequiredArgsConstructor(access = PRIVATE)
    public static final class LayerBuilder {
        private final Consumer<Map<String, Object>> layers;
        private Map<String, Object> values = new LinkedHashMap<>();

        public LayerBuilder add(final String key, final Object value) {
            values.put(key, value);
            return this;
        }

        public void commit() {
            layers.accept(values);
            values = emptyMap();
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    public static final class Layer<DescriptionType>
            extends AbstractMap<String, Object> {
        private final DescriptionType description;
        private final Map<String, Object> values;

        public DescriptionType description() {
            return description;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
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
