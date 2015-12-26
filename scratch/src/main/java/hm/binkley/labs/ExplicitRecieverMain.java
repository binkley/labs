package hm.binkley.labs;

import hm.binkley.labs.ExplicitRecieverMain.Foo.Bar;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @see <a href="http://blog.joda.org/2015/12/explicit-receiver-parameters.html">Explicit
 * receiver parameters</a>
 */
public final class ExplicitRecieverMain {
    public static void main(final String... args) {
        final @Qux String x = "abc";
        final String y = "pqr" + x;

        Stream.of(Foo.class.getDeclaredMethods()).
                map(M::new).
                forEach(out::println);
        out.println("--");
        Stream.of(Bar.class.getConstructors()).
                map(M::new).
                forEach(out::println);
        out.println("--");
        Stream.of((new Baz() {}).getClass().getMethods()).
                filter(m -> Object.class != m.getDeclaringClass()).
                map(M::new).
                forEach(out::println);
        out.println("--");
        Stream.of(((Dil) new Foo()::copy).getClass().getDeclaredMethods()).
                filter(m -> Object.class != m.getDeclaringClass()).
                map(M::new).
                forEach(out::println);
        out.println("--");
        Stream.of(((Dil) new Foo()::copy).getClass().getMethods()).
                filter(m -> Object.class != m.getDeclaringClass()).
                map(M::new).
                forEach(out::println);
    }

    public interface Baz {
        default void nothing(@Qux Baz this) {}
    }

    @FunctionalInterface
    public interface Dil {
        void nothing(@Qux Dil this);
    }

    @Retention(RUNTIME)
    @Target({TYPE_USE, PARAMETER})
    public @interface Qux {}

    @Retention(RUNTIME)
    @Target({TYPE_USE, PARAMETER})
    public @interface Quux {}

    public static final class Foo {
        public void copy(@Qux @Quux Foo this) {}

        public int plain() {
            return 3;
        }

        public class Bar {
            public Bar(@Qux Foo Foo.this) {}

            public Bar(final int plain) {}
        }
    }

    public static final class M {
        private final Executable executable;

        private M(final Executable executable) {
            this.executable = executable;
        }

        @Override
        public String toString() {
            final AnnotatedType receiverType = executable
                    .getAnnotatedReceiverType();
            if (null == receiverType)
                return "[No receiver]\n    " + executable;
            final String receiverAnnotations = Stream
                    .of(receiverType.getAnnotations()).
                            map(Object::toString).
                            collect(Collectors.joining("\n "));
            final StringBuilder buf = new StringBuilder("[");
            if (!receiverAnnotations.isEmpty())
                buf.append(receiverAnnotations).
                        append("\n ");
            buf.append(receiverType.getType().getTypeName()).
                    append("]\n    ").
                    append(executable);
            return buf.toString();
        }
    }
}
