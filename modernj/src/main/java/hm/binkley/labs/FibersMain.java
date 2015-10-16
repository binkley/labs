package hm.binkley.labs;

import co.paralleluniverse.fibers.Fiber;
import lombok.RequiredArgsConstructor;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;
import static java.lang.System.out;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static java.util.Arrays.asList;

/**
 * {@code FibersMain} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class FibersMain {
    public static void main(final String... args)
            throws ExecutionException, InterruptedException,
            MalformedObjectNameException {
        final Fiber<String> fiber = new Fiber<>(() -> "Foo!");
        fiber.start();

        final MBeanServer mbeans = getPlatformMBeanServer();
        quasarBeans(mbeans).stream().
                map(ObjectInstance::getObjectName).
                peek(name -> out.println(name.getCanonicalName())).
                map(name -> new Pair(mbeans, name)).
                forEach(Pair::print);

        out.println(fiber.get());
    }

    private static Collection<ObjectInstance> quasarBeans(
            final MBeanServer mbeans)
            throws MalformedObjectNameException {
        return mbeans.queryMBeans(
                ObjectName.getInstance("co.paralleluniverse:*,type=*"), null);
    }

    @RequiredArgsConstructor
    private static final class Pair {
        private final MBeanServer mbeans;
        private final ObjectName name;

        private void print() {
            try {
                asList(mbeans.getMBeanInfo(name).getAttributes()).stream().
                        map(MBeanFeatureInfo::getName).
                        filter(name -> !"Info".equals(name)).
                        map(this::printable).
                        forEach(out::println);
                out.println("--");
            } catch (final InstanceNotFoundException | IntrospectionException | ReflectionException e) {
                throw new Error(e);
            }
        }

        private String printable(final String a) {
            try {
                final Object value = mbeans.getAttribute(name, a);
                return format("%s: %s", a, printable(value));
            } catch (final RuntimeMBeanException e) {
                // TODO: Why does quasar toss NPE for getRunawayFibers?
                return format("%s: %s", a, e);
            } catch (final MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException e) {
                throw new Error(e);
            }
        }

        private static Object printable(final Object value) {
            if (null == value)
                return null;
            if (!value.getClass().isArray())
                return value;
            return Arrays.toString((long[]) value);
        }
    }
}

