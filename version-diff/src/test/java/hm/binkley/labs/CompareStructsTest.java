package hm.binkley.labs;

import com.fasterxml.jackson.databind.ObjectMapper;
import hm.binkley.labs.FakeCommit.Detail;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static hm.binkley.labs.CompareStructs.compiledCommits;
import static hm.binkley.labs.FakeCommit.loadTestCommits;
import static java.lang.System.out;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.util.UUID.randomUUID;

@SuppressWarnings("StaticNonFinalField")
public final class CompareStructsTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(FAIL_ON_EMPTY_BEANS, false);
    }

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
        repo.create(false);

        final File srcDir = repoDir.newFolder("src", "main", "java");

        writeFakeJavaHistory(repo, srcDir.toPath());
    }

    @Test
    public void should()
            throws IOException {
        compiledCommits(repo, buildDir.getRoot().toPath(),
                CompareStructsTest::processXX);
    }

    private static void processXX(final CompiledCommit compiledCommit) {
        out.println("compiledCommit = " + compiledCommit);
        compiledCommit.compiled.stream().
                peek(c -> out.println(Arrays.toString(c.getFields()))).
                map(CompareStructsTest::bestConstructor).
                peek(out::println).
                map(CompareStructsTest::randomInstance).
                peek(out::println).
                map(CompareStructsTest::toJSON).
                forEach(out::println);
    }

    private static Constructor<?> bestConstructor(final Class<?> type) {
        final Constructor<?>[] ctors = type.getConstructors();
        switch (ctors.length) {
        case 1:
            return ctors[0];
        case 2:
            return 0 == ctors[0].getParameterCount() ? ctors[1] : ctors[0];
        default:
            throw new IllegalArgumentException(
                    "More than 1 or 2 ctors: " + type);
        }
    }

    private static Object randomInstance(final Constructor<?> ctor) {
        try {
            return randomParameters(ctor);
        } catch (final Exception e) {
            throw new IllegalArgumentException(
                    "Cannot instantiate random instance: " + ctor
                            .getDeclaringClass(), e);
        }
    }

    private static Object randomParameters(final Constructor<?> ctor)
            throws IllegalAccessException, InvocationTargetException,
            InstantiationException {
        final Random random = new Random();
        final Object[] params = new Object[ctor.getParameterCount()];
        final Class<?>[] types = ctor.getParameterTypes();
        for (int i = 0, x = params.length; i < x; ++i) {
            final Class<?> type = types[i];
            if (String.class.equals(type))
                params[i] = randomUUID().toString();
            else if (Integer.TYPE.equals(type))
                params[i] = random.nextInt();
            else
                throw new IllegalArgumentException(
                        "Unsupportd type: " + type);
        }
        return ctor.newInstance(params);
    }

    private static String toJSON(final Object o) {
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

        try (final Git git = Git.wrap(repo)) {
            final List<FakeCommit> commits = loadTestCommits();
            out.println("commits = " + commits);

            for (final FakeCommit commit : commits) {
                for (final Detail detail : commit.details) {
                    final File file = new File(repoDir.getRoot(),
                            detail.path);
                    file.delete(); // Copy does not like overwriting
                    try (final InputStream content = detail.getClass()
                            .getResourceAsStream(detail.content)) {
                        copy(content, file.toPath());
                    }
                    git.add().addFilepattern(detail.path).call();
                }
                git.commit().setMessage(commit.message).call();
            }
        }
    }
}
