package hm.binkley.labs;

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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static hm.binkley.labs.CompareStructs.compiledCommits;
import static java.lang.System.out;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;

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
        compiledCommits(repo, buildDir.getRoot().toPath()).stream().
                forEach(out::println);
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
