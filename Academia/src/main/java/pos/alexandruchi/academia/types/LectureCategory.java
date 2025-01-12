package pos.alexandruchi.academia.types;

import java.util.Objects;
import java.util.stream.Stream;

public enum LectureCategory {
    field("domeniu"),
    specialty("specialitate"),
    adjacency("adiacență");

    private final String value;

    LectureCategory(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static LectureCategory of(String value) {
        return Stream.of(LectureCategory.values())
                .filter(p -> Objects.equals(p.toString(), value))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}