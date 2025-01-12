package pos.alexandruchi.academia.types;

import java.util.Objects;
import java.util.stream.Stream;

@SuppressWarnings("SpellCheckingInspection")
public enum TeachingDegree {
    asistent("asist"),
    lecturer("șef lucr"),
    reader("conf"),
    professor("prof");

    private final String value;

    TeachingDegree(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static TeachingDegree of(String value) {
        return Stream.of(TeachingDegree.values())
                .filter(p -> Objects.equals(p.toString(), value))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}