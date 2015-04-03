package hello;

import lombok.Data;

@Data
public class Greeting {
    private long id;
    private String message;

    public Greeting() {
    }

    public Greeting(final long id, final String message) {
        this.id = id;
        this.message = message;
    }
}
