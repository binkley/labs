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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.out;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;

public final class VersionDiffTest {
    public static void main(final String... args)
            throws IOException, GitAPIException {
        final Path rootDir = createTempDirectory("binkley");

        final Path gitDir = rootDir.resolve(".git");
        if (!gitDir.toFile().mkdirs())
            throw new IOException("Cannot make " + gitDir);
        final Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
        repo.create();
        try (final Git git = Git.wrap(repo)) {
            git.close();
            repo.close();
            final Path packageDir = rootDir
                    .resolve(Paths.get("src", "main", "java", "scratch"));
            if (!packageDir.toFile().mkdirs())
                throw new IOException("Cannot make " + packageDir);
            final Path fooFile = packageDir.resolve("Foo.java");

            writeAndCommit(git, fooFile, "First Foo", "package scratch;",
                    "public final class Foo {}");

            writeAndCommit(git, fooFile, "First Foo", "package scratch;",
                    "/** Silly javadoc. */", "public final class Foo {}");
        }

        findStuff(repo);
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

    private static void findStuff(final Repository repo)
            throws IOException {
        final Ref head = repo.getRef("refs/heads/master");
        try (final RevWalk walk = new RevWalk(repo)) {
            final RevCommit commit = walk.parseCommit(head.getObjectId());
            walk.markStart(commit);
            for (final RevCommit rev : walk)
                dumpContents(repo, rev.getId());
            walk.dispose();
        }
    }

    private static void dumpContents(final Repository repo,
            final ObjectId commitId)
            throws IOException {
        try (final RevWalk revWalk = new RevWalk(repo)) {
            final RevCommit commit = revWalk.parseCommit(commitId);
            final RevTree tree = commit.getTree();
            try (final TreeWalk treeWalk = new TreeWalk(repo)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(
                        PathFilter.create("src/main/java/scratch/Foo.java"));

                if (!treeWalk.next())
                    out.println("No Foo.java");
                else {
                    final ObjectId objectId = treeWalk.getObjectId(0);
                    final ObjectLoader loader = repo.open(objectId);
                    loader.copyTo(out);
                }
            }

            revWalk.dispose();
        }
    }
}
