package hm.binkley.labs;

import hm.binkley.labs.ExampleForCobertura.Spinner;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ExampleForCoberturaTest {
    private ExampleForCobertura fixture;

    @Before
    public void setUpFixture() {
        fixture = new ExampleForCobertura(-1, "Bob");
    }

    @Test
    public void shouldSpin() {
        final Spinner spinner = fixture.spin();
        assertThat(spinner.spun, is(false));
    }
}
