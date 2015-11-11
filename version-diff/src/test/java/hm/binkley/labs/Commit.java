package hm.binkley.labs;

import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ToString
public final class Commit {
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

    public String message;
    public final List<Detail> details = new ArrayList<>();
}
