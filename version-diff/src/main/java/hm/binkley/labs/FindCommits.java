package hm.binkley.labs;

import hm.binkley.labs.function.ThrowingConsumer;
import hm.binkley.labs.function.ThrowingFunction;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.ObjectWalk;
import org.eclipse.jgit.revwalk.RevBlob;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.lang.System.out;
import static java.util.Arrays.asList;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType.ADD;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType.COPY;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType.MODIFY;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType.RENAME;
import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

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

    /**
     * The {tree} will return the underlying tree-id instead of the commit-id
     * itself! For a description of what the carets do see e.g. <a
     * href="http://www .paulboxley.com/blog/2011/06/git-caret-and-tilde">xxx</a>
     * This means we are selecting the parent of the parent of the parent of
     * the parent of current HEAD and take the tree-ish of it
     */
    public static void writeCommittedFiles(final Repository repo,
            final ObjectId oldHead, final ObjectId newHead,
            final Function<String, OutputStream> writeTo)
            throws IOException, GitAPIException {
        try (ObjectReader reader = repo.newObjectReader()) {
            final CanonicalTreeParser oldTree = new CanonicalTreeParser();
            oldTree.reset(reader, oldHead);
            final CanonicalTreeParser newTree = new CanonicalTreeParser();
            newTree.reset(reader, newHead);

            try (Git git = new Git(repo)) {
                git.diff().
                        setNewTree(newTree).
                        setOldTree(oldTree).
                        call().stream().
                        filter(FindCommits::skipDeletes).
                        peek(diff -> out.println(
                                format("%s %s", diff.getChangeType(),
                                        diff.getNewPath()))).
                        forEach(diff -> dumpDiff(repo, diff,
                                writeTo.apply(diff.getNewPath())));
            }
        }
    }

    private static boolean skipDeletes(final DiffEntry d) {
        return asList(ADD, COPY, MODIFY, RENAME).
                contains(d.getChangeType());
    }

    private static void dumpDiff(final Repository repo, final DiffEntry diff,
            final OutputStream out) {
        try (final ObjectWalk walk = new ObjectWalk(repo)) {
            final ObjectId objectId = diff.getNewId().toObjectId();
            final RevBlob blob = walk.lookupBlob(objectId);
            walk.parseHeaders(blob);
            walk.parseBody(blob);

            try (final InputStream in = repo.
                    open(objectId, OBJ_BLOB).
                    openStream()) {
                copy(in, out);
            }

            walk.dispose();
        } catch (final IOException e) {
            final IOError x = new IOError(e);
            x.setStackTrace(e.getStackTrace());
            throw x;
        }
    }

    private static void copy(final InputStream in, final OutputStream out)
            throws IOException {
        final byte[] buf = new byte[8192];
        int n;
        while (0 < (n = in.read(buf)))
            out.write(buf, 0, n);
    }
}
