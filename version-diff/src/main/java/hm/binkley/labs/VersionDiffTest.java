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
import org.eclipse.jgit.treewalk.filter.PathFilter;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.walk;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

public final class VersionDiffTest {
    public static void main(final String... args)
            throws IOException, GitAPIException {
        final Path tmpDir = createTempDirectory("binkley");
        // TODO: Recursively delete tmpDir at exit
        final Path repoDir = tmpDir.resolve("repo");
        mkdirs(repoDir);

        final Path buildDir = tmpDir.resolve("build");
        mkdirs(buildDir);

        final Path relativeSrcDir = Paths.get("src", "main", "java");
        final Path srcDir = repoDir.resolve(relativeSrcDir);
        mkdirs(srcDir);
        final Path packageDir = srcDir.resolve(Paths.get("scratch"));
        mkdirs(packageDir);
        final Path fooFile = packageDir.resolve("Foo.java");

        final Path gitDir = repoDir.resolve(".git");
        mkdirs(gitDir);
        final Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
        repo.create();

        try (final Git git = Git.wrap(repo)) {
            git.close();
            repo.close();
            writeAndCommit(git, fooFile, "First Foo", "package scratch;",
                    "public final class Foo {}");

            writeAndCommit(git, fooFile, "First Foo", "package scratch;",
                    "/** Silly javadoc. */", "public final class Foo {}");
        }

        final JavaCompiler javac = getSystemJavaCompiler();
        try (final StandardJavaFileManager files = javac
                .getStandardFileManager(null, null, null)) {
            findStuff(repo, revId -> {
                final List<File> inputs = new ArrayList<>();
                commitContents(repo, revId, revPath -> {
                    final Path srcFile = buildSrcFile(buildDir,
                            relativeSrcDir, revPath);
                    mkdirs(srcFile.getParent());
                    inputs.add(srcFile.toFile());
                    return new FileOutputStream(srcFile.toFile());
                }, "src/main/java/scratch/Foo.java");

                javac.getTask(null, files, null, null, null,
                        files.getJavaFileObjectsFromFiles(inputs)).
                        call();
            });
        }

        walk(buildDir).
                forEach(out::println);
    }

    private static void mkdirs(final Path path)
            throws IOException {
        final File file = path.toFile();
        if (!file.exists() && !file.mkdirs())
            throw new IOException("Cannot make " + path);
    }

    private static Path buildSrcFile(final Path buildDir,
            final Path relativeSrcDir, final String revPath) {
        return buildDir
                .resolve(relativeSrcDir.relativize(Paths.get(revPath)));
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

    private static void findStuff(final Repository repo,
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
            final ObjectId commitId,
            final IOFunction<String, OutputStream> out,
            final String exampleJava)
            throws IOException {
        try (final RevWalk revWalk = new RevWalk(repo)) {
            final RevCommit commit = revWalk.parseCommit(commitId);
            final RevTree tree = commit.getTree();
            try (final TreeWalk treeWalk = new TreeWalk(repo)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(exampleJava));

                if (!treeWalk.next())
                    throw new IOException("No " + exampleJava);
                else {
                    final ObjectId objectId = treeWalk.getObjectId(0);
                    final ObjectLoader loader = repo.open(objectId);
                    try (final OutputStream src = out
                            .apply(treeWalk.getPathString())) {
                        loader.copyTo(src);
                    }
                }
            }

            revWalk.dispose();
        }
    }
}
