package hm.binkley.labs;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static java.lang.System.out;

public final class LayersMain {
    public static void main(final String... args) {
        final Layers<Tag> layers = new Layers<>(Tag.of("Bob", 3));
        out.println("layers = " + layers);
        layers.layer(Tag.of("#1", 13)).commit();
        out.println("layers = " + layers);
        layers.layer(Tag.of("arpha", -2)).
                add("FOO", 13.8d).
                add("BAR", "bAz").
                commit();
        out.println("layers = " + layers);
        layers.layer(Tag.of("bayr", -7)).
                add("BAR", "qUUx").
                commit();
        out.println("layers = " + layers);

        out.println("DUCKY! = " + layers.get("DUCKY!"));
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
}
