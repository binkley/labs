package hm.binkley.labs;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * {@code FindCommits} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public final class FindCommits {
    private FindCommits() {}

    public static void writeOutCommits(final Repository repo,
            final ObjectId commitId, final Consumer<TreeWalk> configure,
            final ThrowingFunction<String, Path, IOException> out)
            throws IOException {
        try (final RevWalk revWalk = new RevWalk(repo)) {
            final RevCommit commit = revWalk.parseCommit(commitId);
            final RevTree tree = commit.getTree();
            try (final TreeWalk treeWalk = new TreeWalk(repo)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                configure.accept(treeWalk);

                while (treeWalk.next()) {
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

    public static void findCommits(final Repository repo,
            final ThrowingConsumer<RevCommit, IOException> process)
            throws IOException {
        final Ref head = repo.getRef("refs/heads/master");
        try (final RevWalk walk = new RevWalk(repo)) {
            final RevCommit commit = walk.parseCommit(head.getObjectId());
            walk.markStart(commit);
            for (final RevCommit rev : walk)
                process.accept(rev);
            walk.dispose();
        }
    }
}
