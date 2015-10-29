package hm.binkley.labs;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nonnull;

import static hm.binkley.labs.ExampleForCobertura.Spinner.spun;
import static hm.binkley.labs.ExampleForCobertura.Spinner.stopped;
import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public class ExampleForCobertura {
    private final int n;
    @Nonnull
    private final String s;

    public Spinner spin() {
        if (0 > n)
            return stopped();
        else if (s.isEmpty())
            return stopped();
        else
            return spun(s);
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor(access = PRIVATE)
    @ToString
    public static final class Spinner {
        public final boolean spun;
        public final String s;

        public static Spinner stopped() {
            return new Spinner(false, null);
        }

        public static Spinner spun(final String s) {
            return new Spinner(true, s);
        }
    }
}
