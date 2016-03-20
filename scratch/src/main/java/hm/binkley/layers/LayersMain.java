package hm.binkley.layers;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static hm.binkley.layers.Layers.defaultRule;
import static hm.binkley.layers.LayersMain.Key.BAR;
import static hm.binkley.layers.LayersMain.Key.DUCKY;
import static hm.binkley.layers.LayersMain.Key.FOO;
import static java.lang.System.out;

public final class LayersMain {
    public static void main(final String... args) {
        final Layers<Tag, Key> layers = new Layers<>(Tag.of("Bob", 3),
                defaultRule(Tag.of("Default rule - take last", 0)));
        out.println("layers = " + layers);
        layers.layer(Tag.of("#1", 13)).commit();
        out.println("layers = " + layers);
        layers.layer(Tag.of("arpha", -2)).
                add(FOO, 13.8d).
                add(BAR, "bAz").
                commit();
        out.println("layers = " + layers);
        layers.layer(Tag.of("bayr", -7)).
                add(BAR, "qUUx").
                commit();
        out.println("layers = " + layers);

        out.println("FOO = " + layers.get(FOO));
        out.println("DUCKY! = " + layers.get(DUCKY));
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor(staticName = "of")
    public static final class Tag {
        public final String name;
        public final int number;

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + name + ", " + number
                    + ")";
        }
    }

    public enum Key {
        FOO("A fooish thing"),
        BAR("Bar none"),
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
