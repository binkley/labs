package hm.binkley.labs;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOError;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

/**
 * {@code CompileJava} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public final class CompileJava {
    private static final JavaCompiler javac = getSystemJavaCompiler();

    private CompileJava() {}

    public static void processCompiledJava(
            final IOConsumer<StandardJavaFileManager> process)
            throws IOException {
        try (final StandardJavaFileManager manager = javac
                .getStandardFileManager(null, null, null)) {
            process.accept(manager);
        }
    }

    public static List<Class<?>> loadClasses(final Path buildDir,
            final StandardJavaFileManager files, final String className,
            final Path srcFile) {
        return stream(
                files.getJavaFileObjects(srcFile.toFile()).spliterator(),
                false).
                map(objFile -> new SimpleImmutableEntry<>(objFile,
                        compile(files, objFile))).
                filter(Entry::getValue).
                map(e -> loadClass(buildDir, className)).
                collect(toList());
    }

    private static Class<?> loadClass(final Path buildDir,
            final String className) {
        try (final URLClassLoader loader = newLoader(buildDir)) {
            return loader.loadClass(className);
        } catch (final IOException | ClassNotFoundException e) {
            throw new IOError(e);
        }
    }

    private static URLClassLoader newLoader(final Path buildDir)
            throws MalformedURLException {
        return new URLClassLoader(
                new URL[]{buildDir.toFile().toURI().toURL()});
    }

    private static Boolean compile(final StandardJavaFileManager files,
            final JavaFileObject objFile) {
        return javac.
                getTask(null, files, null, null, null, singleton(objFile)).
                call();
    }
}
