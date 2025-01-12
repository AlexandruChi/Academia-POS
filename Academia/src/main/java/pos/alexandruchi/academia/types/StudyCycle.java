package pos.alexandruchi.academia.types;

import java.util.Objects;
import java.util.stream.Stream;

public enum StudyCycle {
    bachelor("licență"),
    master("master");

    private final String value;

    StudyCycle(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static StudyCycle of(String value) {
        return Stream.of(StudyCycle.values())
                .filter(p -> Objects.equals(p.toString(), value))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}