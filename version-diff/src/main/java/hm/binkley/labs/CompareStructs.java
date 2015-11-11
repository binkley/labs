package hm.binkley.labs;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static hm.binkley.labs.CompileJava.loadClasses;
import static java.nio.file.Files.createDirectories;

public final class CompareStructs {
    private static final Pattern javaSuffix = Pattern.compile("\\.java$");
    private static final Path relativeSrcDir = Paths
            .get("src", "main", "java");

    static void configureTreeWalk(final TreeWalk treeWalk) {
        treeWalk.setFilter(PathSuffixFilter.create(".java"));
    }

    static CompiledCommit compile(final Path buildDir, final RevCommit commit,
            final StandardJavaFileManager files, final String revPath)
            throws IOException {
        final Path relativeSrcPath = relativeSrcDir
                .relativize(Paths.get(revPath));
        final Path srcFile = buildDir.resolve(relativeSrcPath);
        createDirectories(srcFile.getParent());

        return CompiledCommit.of(commit, srcFile,
                loadClasses(buildDir, files, toJavaName(relativeSrcPath),
                        srcFile));
    }

    private static String toJavaName(final Path relativeSrcPath) {
        // TODO: Correct binary name!  Ex: inner classes are scratch.Foo$Bar
        return javaSuffix.
                matcher(relativeSrcPath.toString()).replaceAll("").
                replace('\\', '.').
                replace('/', '.');
    }
}
