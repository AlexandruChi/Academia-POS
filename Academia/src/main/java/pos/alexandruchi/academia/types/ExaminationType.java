package pos.alexandruchi.academia.types;

import java.util.Objects;
import java.util.stream.Stream;

public enum ExaminationType {
    exam("examen"),
    colloquy("colocviu");

    private final String value;

    ExaminationType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ExaminationType of(String value) {
        return Stream.of(ExaminationType.values())
                .filter(p -> Objects.equals(p.toString(), value))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}