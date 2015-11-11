package hm.binkley.labs;

import com.fasterxml.jackson.databind.ObjectMapper;
import hm.binkley.util.Bug;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.function.Supplier;

import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static hm.binkley.labs.CompareStructs.compiledCommits;
import static hm.binkley.util.function.Matching.matching;
import static java.lang.System.out;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("StaticNonFinalField")
public final class CompareStructsTest {
    @ClassRule
    public static final TemporaryFolder repoDir = new TemporaryFolder();

    private static Repository repo;

    @Rule
    public final TemporaryFolder buildDir = new TemporaryFolder();

    @BeforeClass
    public static void setUpRepo()
            throws IOException, GitAPIException {
        final File gitDir = repoDir.newFolder(".git");
        repo = FileRepositoryBuilder.create(gitDir);
        repo.create();
        final File srcDir = repoDir.newFolder("src", "main", "java");

        writeFakeJavaHistory(repo, srcDir.toPath());
    }

    @Test
    public void should()
            throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(FAIL_ON_EMPTY_BEANS, false);

        compiledCommits(repo, buildDir.getRoot().toPath()).stream().
                flatMap(cc -> cc.compiled.stream()).
                map(CompareStructsTest::bestConstructor).
                map(CompareStructsTest::randomInstance).
                map(o -> toJSON(mapper, o)).
                forEach(out::println);
    }

    private static Constructor<? extends Object> bestConstructor(
            final Class<?> c) {
        final Constructor<?>[] ctors = c.getConstructors();
        switch (ctors.length) {
        case 1:
            return ctors[0];
        case 2:
            return 0 == ctors[0].getParameterCount() ? ctors[1] : ctors[0];
        default:
            throw new Bug("More than 1 or 2 ctors: %s", c);
        }
    }

    private static Object randomInstance(final Constructor<?> ctor) {
        try {
            return randomParameters(ctor);
        } catch (final Exception e) {
            throw new Bug(e, "Cannot instantiate random %s",
                    ctor.getDeclaringClass());
        }
    }

    private static Object randomParameters(final Constructor<?> ctor)
            throws IllegalAccessException, InvocationTargetException,
            InstantiationException {
        final Random random = new Random();
        return ctor.newInstance(asList(ctor.getParameterTypes()).stream().
                map(type -> matching(Class.class, Object.class).
                        when(String.class::equals).
                        then(() -> randomUUID().toString()).
                        when(int.class::equals).
                        then((Supplier<Object>) random::nextInt).
                        none().thenThrow(
                        () -> new Bug("Unsupported type: %s", type))).
                collect(toList()).
                toArray(new Object[ctor.getParameterCount()]));
    }

    private static String toJSON(final ObjectMapper mapper, final Object o) {
        try {
            final StringWriter writer = new StringWriter();
            mapper.writeValue(writer, o);
            return writer.toString();
        } catch (final IOException e) {
            throw new IOError(e);
        }
    }

    private static void writeFakeJavaHistory(final Repository repo,
            final Path srcDir)
            throws IOException, GitAPIException {
        final Path packageDir = srcDir.resolve(Paths.get("scratch"));
        createDirectories(packageDir);
        final Path fooFile = packageDir.resolve("Foo.java");
        final Path barFile = packageDir.resolve("Bar.java");

        try (final Git git = Git.wrap(repo)) {
            writeAndCommit(git, fooFile, "Init", "package scratch;",
                    "public final class Foo {",
                    "    public static void main(final String... args) {",
                    "    }", "}");

            writeAndCommit(git, fooFile, "No real change", "package scratch;",
                    "/** Silly javadoc. */", "public final class Foo {",
                    "    public static void main(final String... args) {",
                    "    }", "}");

            writeAndCommit(git, fooFile, "Added field", "package scratch;",
                    "/** Silly javadoc. */", "public final class Foo {",
                    "    public final int x = 4;",
                    "    public static void main(final String... args) {",
                    "    }", "}");

            writeAndCommit(git, fooFile, null, "package scratch;",
                    "/** Another change. */", "public final class Foo {",
                    "    public final int x = 4;",
                    "    public static void main(final String... args) {",
                    "    }", "}");

            writeAndCommit(git, barFile, "Two files in one commit",
                    "package scratch;", "/** Another change. */",
                    "public final class Bar {}");

            writeAndCommit(git, fooFile, null, "package scratch;",
                    "public final class Foo {", "    public final int x = 4;",
                    "    public static void main(final String... args) {",
                    "    }", "}");

            writeAndCommit(git, barFile,
                    "Fake change - first commit ignored?", "package scratch;",
                    "public final class Bar {}");
        }
    }

    private static void writeAndCommit(final Git git, final Path where,
            @Nullable final String commitMessage, final String... lines)
            throws IOException, GitAPIException {
        write(where, asList(lines));
        git.add().
                addFilepattern("src").
                call();
        if (null != commitMessage)
            git.commit().
                    setMessage(commitMessage).
                    call();
    }
}
