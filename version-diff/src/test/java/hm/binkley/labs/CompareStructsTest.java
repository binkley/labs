package hm.binkley.labs;

import com.fasterxml.jackson.databind.ObjectMapper;
import hm.binkley.labs.Commit.Detail;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static hm.binkley.labs.CompareStructs.compiledCommits;
import static hm.binkley.util.function.Matching.matching;
import static java.lang.System.out;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
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
        repo.create(false);

        final File srcDir = repoDir.newFolder("src", "main", "java");

        writeFakeJavaHistory(repo, srcDir.toPath());
    }

    @Test
    public void should()
            throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(FAIL_ON_EMPTY_BEANS, false);

        compiledCommits(repo, buildDir.getRoot().toPath()).stream().
                peek(out::println).
                flatMap(cc -> cc.compiled.stream()).
                peek(c -> out.println(Arrays.toString(c.getFields()))).
                map(CompareStructsTest::bestConstructor).
                map(CompareStructsTest::randomInstance).
                map(o -> toJSON(mapper, o)).
                forEach(out::println);
    }

    private static Constructor<?> bestConstructor(final Class<?> c) {
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

        try (final Git git = Git.wrap(repo)) {
            final List<Commit> commits = loadTestCommits();
            out.println("commits = " + commits);

            for (final Commit commit : commits) {
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

    private static List<Commit> loadTestCommits()
            throws IOException {
        final List<Commit> commits = new ArrayList<>();
        final ResourcePatternResolver loader
                = new PathMatchingResourcePatternResolver();
        final List<Resource> resources = asList(
                loader.getResources("classpath:/commits/*.yml"));
        resources.sort((a, b) -> {
            final int i = Integer.valueOf(a.getFilename()
                    .substring(0, a.getFilename().indexOf(".")));
            final int j = Integer.valueOf(b.getFilename()
                    .substring(0, b.getFilename().indexOf(".")));
            return Integer.compare(i, j);
        });

        final Yaml yaml = new Yaml();
        for (final Resource resource : resources)
            try (final InputStream in = resource.getInputStream()) {
                commits.add(yaml.loadAs(in, Commit.class));
            }

        return commits;
    }
}
