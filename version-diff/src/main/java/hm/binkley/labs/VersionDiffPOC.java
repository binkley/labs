package hm.binkley.labs;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.lang.System.out;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.walk;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

public final class VersionDiffPOC {
    private static final Function newList = __ -> new ArrayList<>();
    private static final Pattern javaSuffix = Pattern.compile("\\.java$");

    @SuppressWarnings("unchecked")
    private static <K, V> Function<K, List<V>> newList() {
        return newList;
    }

    public static void main(final String... args)
            throws IOException, GitAPIException {
        // TODO: Recursively delete tmpDir at exit
        final Path tmpDir = createTempDirectory("binkley");

        final Path repoDir = tmpDir.resolve("repo");
        mkdirs(repoDir);
        final Path gitDir = repoDir.resolve(".git");
        mkdirs(gitDir);
        final Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
        repo.create();

        final Path relativeSrcDir = Paths.get("src", "main", "java");
        final Path srcDir = repoDir.resolve(relativeSrcDir);
        mkdirs(srcDir);

        writeFakeJavaHistory(repo, srcDir);

        final Path buildDir = tmpDir.resolve("build");
        mkdirs(buildDir);
        final Map<Path, List<Class>> compiled = new ConcurrentHashMap<>();
        final JavaCompiler javac = getSystemJavaCompiler();
        try (final StandardJavaFileManager files = javac
                .getStandardFileManager(null, null, null)) {
            findCommits(repo, revId -> {
                commitContents(repo, revId, revPath -> {
                    final Path relativeSrcPath = relativeSrcDir
                            .relativize(Paths.get(revPath));
                    final String className = toJavaName(relativeSrcPath);
                    final Path srcFile = buildDir.resolve(relativeSrcPath);
                    mkdirs(srcFile.getParent());
                    compiled.computeIfAbsent(relativeSrcPath, newList()).
                            addAll(stream(
                                    files.getJavaFileObjects(srcFile.toFile())
                                            .spliterator(), false).
                                    map(objFile -> new SimpleImmutableEntry<>(
                                            objFile,
                                            compile(javac, files, objFile))).
                                    filter(Entry::getValue).
                                    map(e -> {
                                        try (final URLClassLoader loader = new URLClassLoader(
                                                new URL[]{buildDir.toFile()
                                                        .toURI().toURL()})) {
                                            return loader
                                                    .loadClass(className);
                                        } catch (final Exception x) {
                                            throw new IOError(x);
                                        }
                                    }).
                                    collect(toList()));
                    return srcFile;
                });

                walk(buildDir).
                        filter(p -> p.toFile().isFile()).
                        forEach(out::println);
            });

            out.println("compiled = " + compiled);

            compiled.values().stream().
                    flatMap(Collection::stream).
                    map(c -> Arrays.toString(c.getFields())).
                    forEach(out::println);
        }
    }

    private static String toJavaName(final Path relativeSrcPath) {
        // TODO: Correct binary name!  Ex: inner classes are scratch.Foo$Bar
        return javaSuffix.
                matcher(relativeSrcPath.toString()).replaceAll("").
                replace('/', '.');
    }

    private static Boolean compile(final JavaCompiler javac,
            final StandardJavaFileManager files,
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
        }
    }

    private static void mkdirs(final Path path)
            throws IOException {
        final File file = path.toFile();
        if (!file.exists() && !file.mkdirs())
            throw new IOException("Cannot make " + path);
    }

    @FunctionalInterface
    public interface IOConsumer<T> {
        void accept(final T in)
                throws IOException;
    }

    private static void writeAndCommit(final Git git, final Path where,
            final String commitMessage, final String... lines)
            throws IOException, GitAPIException {
        write(where, asList(lines));
        git.add().
                addFilepattern("src").
                call();
        git.commit().
                setMessage(commitMessage).
                call();
    }

    private static void findCommits(final Repository repo,
            final IOConsumer<ObjectId> process)
            throws IOException {
        final Ref head = repo.getRef("refs/heads/master");
        try (final RevWalk walk = new RevWalk(repo)) {
            final RevCommit commit = walk.parseCommit(head.getObjectId());
            walk.markStart(commit);
            for (final RevCommit rev : walk)
                process.accept(rev.getId());
            walk.dispose();
        }
    }

    @FunctionalInterface
    public interface IOFunction<T, U> {
        U apply(final T in)
                throws IOException;
    }

    private static void commitContents(final Repository repo,
            final ObjectId commitId, final IOFunction<String, Path> out)
            throws IOException {
        try (final RevWalk revWalk = new RevWalk(repo)) {
            final RevCommit commit = revWalk.parseCommit(commitId);
            final RevTree tree = commit.getTree();
            try (final TreeWalk treeWalk = new TreeWalk(repo)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathSuffixFilter.create(".java"));

                if (!treeWalk.next())
                    throw new IOException("No Java");
                else {
                    final ObjectId objectId = treeWalk.getObjectId(0);
                    final ObjectLoader loader = repo.open(objectId);
                    try (final OutputStream src = new FileOutputStream(
                            out.apply(treeWalk.getPathString()).toFile())) {
                        loader.copyTo(src);
                    }
                }
            }

            revWalk.dispose();
        }
    }
}
