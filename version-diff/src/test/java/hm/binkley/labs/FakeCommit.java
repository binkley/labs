package hm.binkley.labs;

import lombok.ToString;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

@ToString
public final class FakeCommit {
    public String message;
    public final List<Detail> details = new ArrayList<>();

    public static List<FakeCommit> readFakeCommits()
            throws IOException {
        final List<FakeCommit> commits = new ArrayList<>();
        final ResourcePatternResolver loader
                = new PathMatchingResourcePatternResolver();
        final List<Resource> resources = asList(
                loader.getResources("classpath:/commits/*.yml"));
        resources.sort((a, b) -> {
            final int i = Integer.valueOf(a.getFilename()
                    .substring(0, a.getFilename().indexOf(".")));
            final int j = Integer.valueOf(b.getFilename()
                    .substring(0, b.getFilename().indexOf(".")));
            return Integer.compare(i, j);
        });

        final Yaml yaml = new Yaml();
        for (final Resource resource : resources)
            try (final InputStream in = resource.getInputStream()) {
                commits.add(yaml.loadAs(in, FakeCommit.class));
            }

        return commits;
    }

    @ToString
    public static final class Detail {
        public String path;
        public String content;

        public void read(final Consumer<InputStream> reader)
                throws IOException {
            try (final InputStream in = getClass()
                    .getResourceAsStream(content)) {
                reader.accept(in);
            }
        }
    }
}
