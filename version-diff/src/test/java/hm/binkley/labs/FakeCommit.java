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
final class FakeCommit {
    private static final String YAML_RESOURCES = "classpath:/commits/*.yml";
    private static final ResourcePatternResolver loader
            = new PathMatchingResourcePatternResolver();
    private static final Yaml yaml = new Yaml();

    static {
    }

    String message;
    final List<Detail> details = new ArrayList<>();

    static List<FakeCommit> readFakeCommits()
            throws IOException {
        final List<FakeCommit> commits = new ArrayList<>();
        for (final Resource resource : loadYamlResources())
            try (final InputStream in = resource.getInputStream()) {
                commits.add(yaml.loadAs(in, FakeCommit.class));
            }
        return commits;
    }

    private static List<Resource> loadYamlResources()
            throws IOException {
        final List<Resource> resources = asList(
                loader.getResources(YAML_RESOURCES));
        resources.sort(FakeCommit::sortByPrefixedNumber);
        return resources;
    }

    private static int sortByPrefixedNumber(final Resource a,
            final Resource b) {
        final int i = Integer.parseInt(
                a.getFilename().substring(0, a.getFilename().indexOf(".")));
        final int j = Integer.parseInt(
                b.getFilename().substring(0, b.getFilename().indexOf(".")));
        return Integer.compare(i, j);
    }

    @ToString
    static final class Detail {
        String path;
        String content;

        void read(final Consumer<InputStream> reader)
                throws IOException {
            try (final InputStream in = getClass()
                    .getResourceAsStream(content)) {
                reader.accept(in);
            }
        }
    }
}
