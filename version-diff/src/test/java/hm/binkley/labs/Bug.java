package hm.binkley.labs;

import org.intellij.lang.annotations.PrintFormat;

import static java.lang.String.format;

final class Bug
        extends RuntimeException {
    @PrintFormat
    Bug(final String message, final Object... args) {
        super(format(message, args), null);
    }

    @PrintFormat
    Bug(final Throwable cause, final String message, final Object... args) {
        super(format(message, args), cause);
    }
}
