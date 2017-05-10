package hm.binkley.labs;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
@ToString
public final class CompiledCommit {
    @Nonnull
    public final RevCommit commit;
    @Nullable
    public final Path srcFile;
    @Nonnull
    public final List<Class<?>> compiled;

    public static CompiledCommit of(@Nonnull final RevCommit commit,
            @Nullable final Path srcFile,
            @Nonnull final List<Class<?>> compiled) {
        return new CompiledCommit(commit, srcFile,
                compiled.isEmpty() ? emptyList()
                        : unmodifiableList(compiled));
    }
}
