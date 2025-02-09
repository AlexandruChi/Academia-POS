package pos.alexandruchi.academia.types;

import java.util.Objects;
import java.util.stream.Stream;

public enum AssociationType {
    holder("titular"),
    associate("asociat"),
    external("extern");

    private final String value;

    AssociationType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static AssociationType of(String value) {
        if (value == null || value.isBlank()) {
            return external;
        }

        return Stream.of(AssociationType.values())
                .filter(p -> Objects.equals(p.toString(), value))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}