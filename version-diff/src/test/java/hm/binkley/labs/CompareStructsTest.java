package hm.binkley.labs;

import com.fasterxml.jackson.databind.ObjectMapper;
import hm.binkley.labs.FakeCommit.Detail;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static hm.binkley.labs.CompareStructs.compiledCommits;
import static hm.binkley.labs.FakeCommit.readFakeCommits;
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
    private static Git git;

    @Rule
    public final TemporaryFolder buildDir = new TemporaryFolder();

    @BeforeClass
    public static void fakeRepository()
            throws IOException, GitAPIException {
        final File gitDir = repoDir.newFolder(".git");
        repo = FileRepositoryBuilder.create(gitDir);
        repo.create(false);
        git = Git.wrap(repo);

        final File srcDir = repoDir.newFolder("src", "main", "java");

        writeFakeJavaHistory(srcDir.toPath());
    }

    @AfterClass
    public static void closeRepository() {
        git.close();
        repo.close();
    }

    @Test
    public void should()
            throws IOException {
        compiledCommits(repo, buildDir.getRoot().toPath(),
                CompareStructsTest::generateJson);
    }

    private static void generateJson(final CompiledCommit compiledCommit) {
        out.println(compiledCommit.compiled.stream().
                map(CompareStructsTest::bestConstructor).
                map(CompareStructsTest::randomInstance).
                map(CompareStructsTest::toJSON).
                collect(MappedJson::new, MappedJson::add, Map::putAll));
    }

    private static TypedValue<Constructor> bestConstructor(
            final Class<?> type) {
        final Constructor[] ctors = type.getConstructors();
        switch (ctors.length) {
        case 1:
            return TypedValue.of(ctors[0], type);
        case 2:
            return TypedValue.of(0 == ctors[0].getParameterCount() ? ctors[1]
                    : ctors[0], type);
        default:
            throw new Bug("More than 1 or 2 ctors: %s", type);
        }
    }

    private static TypedValue<Object> randomInstance(
            final TypedValue<Constructor> ctor) {
        try {
            return ctor.map(CompareStructsTest::randomParameters);
        } catch (final Exception e) {
            throw new Bug(e, "Cannot instantiate random instance: %s",
                    ctor.value.getDeclaringClass());
        }
    }

    private static Object randomParameters(final Constructor ctor)
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
                params[i] = random.nextInt(128);
            else
                throw new Bug("Unsupported ctor param type: %s", type);
        }
        return ctor.newInstance(params);
    }

    private static TypedValue<String> toJSON(final TypedValue<Object> o) {
        try {
            return o.map(value -> {
                final StringWriter writer = new StringWriter();
                mapper.writeValue(writer, o.value);
                return writer.toString();
            });
        } catch (final IOException e) {
            throw new Bug(e, "Cannot write JSON: %s", o);
        }
    }

    private static void writeFakeJavaHistory(final Path srcDir)
            throws IOException, GitAPIException {
        final Path packageDir = srcDir.resolve(Paths.get("scratch"));
        createDirectories(packageDir);

        for (final FakeCommit commit : readFakeCommits()) {
            for (final Detail detail : commit.details) {
                final File file = new File(repoDir.getRoot(), detail.path);
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

    private static final class MappedJson
            extends ConcurrentHashMap<Class, List<String>> {
        public void add(final TypedValue<String> json) {
            computeIfAbsent(json.relatedTo, kv -> new ArrayList<>())
                    .add(json.value);
        }
    }
}
