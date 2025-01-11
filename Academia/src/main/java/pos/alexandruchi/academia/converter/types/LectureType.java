package pos.alexandruchi.academia.converter.types;

import java.util.Objects;
import java.util.stream.Stream;

public enum LectureType {
    mandatory("impusă"),
    optional("opțională"),
    freelyChosen("liber_aleasă");

    private final String value;

    LectureType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static LectureType of(String value) {
        return Stream.of(LectureType.values())
                .filter(p -> Objects.equals(p.toString(), value))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
