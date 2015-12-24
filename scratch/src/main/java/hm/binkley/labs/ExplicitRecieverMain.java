package hm.binkley.labs;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.Stream;

import static java.lang.System.out;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

/**
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @see <a href="http://blog.joda.org/2015/12/explicit-receiver-parameters.html">Explicit
 * receiver parameters</a>
 */
public final class ExplicitRecieverMain {
    public static void main(final String... args) {
        Stream.of(Foo.class.getMethods()).
                map(M::new).
                forEach(out::println);
    }

    @Retention(RUNTIME)
    @Target(TYPE_USE)
    public @interface Bar {}

    public static final class Foo {
        public Foo copy(@Bar Foo this) {
            return this;
        }
    }

    public static final class M {
        private final Method method;
        private final Collection<Annotation> annotations;

        private M(final Method method) {
            this.method = method;
            annotations = asList(
                    method.getAnnotatedReceiverType().getAnnotations());
        }

        @Override
        public String toString() {
            final String annotations = this.annotations.stream().
                    map(Object::toString).
                    collect(joining("\n"));
            return annotations.isEmpty() ? method.toString()
                    : annotations + "\n" + method;
        }
    }
}
