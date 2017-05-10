package hm.binkley.labs;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;

import static hm.binkley.labs.FindCommits.writeCommittedFiles;
import static java.lang.System.out;

/**
 * {@code XXX} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public class XXX {
    public static void main(final String... args)
            throws IOException, GitAPIException {
        try (final Repository repo = new FileRepository(
                "/Users/boxley/src/java/labs/.git")) {
            writeCommittedFiles(repo, repo.resolve("HEAD^^^^{tree}"),
                    repo.resolve("HEAD^{tree}"), path -> out);
        }
    }
}
