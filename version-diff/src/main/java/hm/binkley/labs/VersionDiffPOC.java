package hm.binkley.labs;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import javax.annotation.Nullable;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static hm.binkley.labs.FindCommits.findCommits;
import static hm.binkley.labs.FindCommits.writeOutCommits;
import static hm.binkley.labs.IORunnable.rethrow;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.out;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

public final class VersionDiffPOC {
    private static final Pattern javaSuffix = Pattern.compile("\\.java$");
    private static final Path relativeSrcDir = Paths
            .get("src", "main", "java");
    private static final JavaCompiler javac = getSystemJavaCompiler();

    public static void main(final String... args)
            throws IOException, GitAPIException {
        // TODO: Recursively delete tmpDir at exit
        final Path tmpDir = createTempDirectory(
                VersionDiffPOC.class.getPackage().getName());
        getRuntime().addShutdownHook(
                new Thread(rethrow(() -> recursivelyDelete(tmpDir))));

        final Path repoDir = tmpDir.resolve("repo");
        mkdirs(repoDir);
        final Path gitDir = repoDir.resolve(".git");
        mkdirs(gitDir);
        final Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
        repo.create();

        final Path srcDir = repoDir.resolve(relativeSrcDir);
        mkdirs(srcDir);

        writeFakeJavaHistory(repo, srcDir);

        final Path buildDir = tmpDir.resolve("build");
        final List<CompiledCommit> commits = new ArrayList<>();

        try (final StandardJavaFileManager files = javac
                .getStandardFileManager(null, null, null)) {
            findCommits(repo, commit -> writeOutCommits(repo, commit.getId(),
                    VersionDiffPOC::configureTreeWalk,
                    revPath -> compile(buildDir, commit, commits, files,
                            revPath)));
        }

        out.println("commits = " + commits);
    }

    private static Path recursivelyDelete(final Path tmpDir)
            throws IOException {
        return walkFileTree(tmpDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file,
                    final BasicFileAttributes attrs)
                    throws IOException {
                delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir,
                    final IOException exc)
                    throws IOException {
                delete(dir);
                return CONTINUE;
            }
        });
    }

    private static void configureTreeWalk(final TreeWalk treeWalk) {
        treeWalk.setFilter(PathSuffixFilter.create(".java"));
    }

    private static Path compile(final Path buildDir, final RevCommit commit,
            final List<CompiledCommit> commits,
            final StandardJavaFileManager files, final String revPath)
            throws IOException {
        final Path relativeSrcPath = relativeSrcDir
                .relativize(Paths.get(revPath));
        final Path srcFile = buildDir.resolve(relativeSrcPath);
        mkdirs(srcFile.getParent());
        final CompiledCommit compiledCommit = CompiledCommit.of(commit,
                loadClasses(buildDir, files, toJavaName(relativeSrcPath),
                        srcFile));
        commits.add(compiledCommit);
        return srcFile;
    }

    private static List<Class<?>> loadClasses(final Path buildDir,
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

    private static String toJavaName(final Path relativeSrcPath) {
        // TODO: Correct binary name!  Ex: inner classes are scratch.Foo$Bar
        return javaSuffix.
                matcher(relativeSrcPath.toString()).replaceAll("").
                replace('/', '.');
    }

    private static Boolean compile(final StandardJavaFileManager files,
            final JavaFileObject objFile) {
        return javac.
                getTask(null, files, null, null, null, singleton(objFile)).
                call();
    }

    private static void writeFakeJavaHistory(final Repository repo,
            final Path srcDir)
            throws IOException, GitAPIException {
        final Path packageDir = srcDir.resolve(Paths.get("scratch"));
        mkdirs(packageDir);
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

    private static void mkdirs(final Path path)
            throws IOException {
        final File file = path.toFile();
        if (!file.exists() && !file.mkdirs())
            throw new IOException("Cannot make " + path);
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
