package hm.binkley.labs;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * {@code ExampleStruct} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "of")
@ToString
public final class ExampleStruct {
    public final int n;
    public final String s;
}
