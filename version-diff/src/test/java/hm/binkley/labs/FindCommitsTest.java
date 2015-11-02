package hm.binkley.labs;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static hm.binkley.labs.FindCommits.findCommits;
import static hm.binkley.labs.FindCommits.writeOutCommits;
import static java.nio.file.Files.lines;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code FindCommitsTest} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
public class FindCommitsTest {
    @Rule
    public final TemporaryFolder repoDir = new TemporaryFolder();

    private Repository repo;
    private Git git;

    @Before
    public void setUpRepo()
            throws IOException {
        final Path repoDir = this.repoDir.getRoot().toPath();
        final Path gitDir = repoDir.resolve(".git");
        if (!gitDir.toFile().mkdirs())
            throw new IOException("Cannot make " + gitDir);
        repo = FileRepositoryBuilder.create(gitDir.toFile());
        repo.create();
        git = new Git(repo);
    }

    @After
    public void tearDownRepo() {
        git.close();
        repo.close();
    }

    @Test
    public void shouldGetContentsOfFirstCommit()
            throws IOException, GitAPIException {
        final File readme = repoDir.newFile("README.md");
        final String firstLine = "# README";
        write(readme.toPath(), asList(firstLine));
        git.add().
                addFilepattern("README.md").
                call();
        git.commit().
                setMessage("First post.").
                call();

        findCommits(repo,
                commit -> writeOutCommits(repo, commit.getId(), treeWalk -> {
                }, revPath -> repoDir.getRoot().toPath().resolve(revPath)));

        assertThat(
                lines(new File(repoDir.getRoot(), "README" + ".md").toPath()).
                        collect(toList()),
                is(equalTo(singletonList(firstLine))));
    }
}
