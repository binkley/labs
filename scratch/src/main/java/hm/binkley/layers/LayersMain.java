package hm.binkley.layers;

import hm.binkley.layers.Layers.Rule;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static hm.binkley.layers.Layers.withFallbackRule;
import static hm.binkley.layers.LayersMain.Key.*;
import static java.lang.System.out;

public final class LayersMain {
    public static void main(final String... args) {
        final Layers<Tag, Key, Value> layers = withFallbackRule(
                Tag.of("Bob", 3), Tag.of("Default rule - take last", 0));
        out.println("EMPTY layers = " + layers);
        layers.layer(Tag.of("#1", 13)).commit();
        out.println("STILL EMPTY layers = " + layers);
        layers.layer(Tag.of("arpha", -2)).
                add(FOO, new Value() {
                    @Override
                    public String toString() {
                        return Double.toString(14.4d);
                    }
                }).
                add(BAR, new Value() {
                    @Override
                    public String toString() {
                        return "bAz";
                    }
                }).
                commit();
        out.println("TWO layers = " + layers);
        layers.layer(Tag.of("bayr", -7)).
                add(BAR, new Value() {
                    @Override
                    public String toString() {
                        return "qUUx";
                    }
                }).
                commit();
        out.println("STILL TWO layers = " + layers);

        layers.addRule(BAR, new Rule<>(Tag.of("Bad FOO, no biscuit!", 1)) {
            @Override
            public Value apply(final Value a, final Value b) {
                return a;
            }
        });
        out.println("REVERSE BAR layers = " + layers);

        out.println("FOO = " + layers.get(FOO));
        out.println("DUCKY! = " + layers.get(DUCKY));

        final Layers<String, String, Object> vanilla = withFallbackRule(
                "Very withFallbackRule", "Last first");
        out.println("withFallbackRule = " + vanilla);
    }

    private interface Value {
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor(staticName = "of")
    private static final class Tag {
        public final String name;
        public final int number;

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + name + ", " + number +
                    ")";
        }
    }

    enum Key {
        FOO("A fooish thing"), BAR("Bar none"),
        DUCKY("Pearl misses you, Ducky!");

        private final String description;

        Key(final String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return name() + ": " + description;
        }
    }
}
