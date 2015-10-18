package hm.binkley.labs;

import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.mPERs;
import org.checkerframework.checker.units.qual.s;

import static java.lang.System.out;

/**
 * {@code CustomTypesMain} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class CustomTypesMain {
    public static void main(final String... args) {
        @SuppressWarnings("unsafe") final @m int m = 1; // define 1 meter
        @SuppressWarnings("unsafe") final @s int s = 1; // define 1 second

        @m final double meters = 5.0 * m;
        @s final double seconds = 2.0 * s;
        // @kmPERh double speed = meters / seconds; // <-- doesn't compile
        @mPERs final double speed = meters / seconds;

        out.println("Speed: " + speed);
    }
}
