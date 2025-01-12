package pos.alexandruchi.academia.types.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pos.alexandruchi.academia.types.StudyCycle;

@Converter(autoApply = true)
public class StudyCycleConverter implements AttributeConverter<StudyCycle, String> {
    @Override
    public String convertToDatabaseColumn(StudyCycle studyCycle) {
        return studyCycle.toString();
    }

    @Override
    public StudyCycle convertToEntityAttribute(String s) {
        return StudyCycle.of(s);
    }
}
