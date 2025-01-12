package pos.alexandruchi.academia.types.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pos.alexandruchi.academia.types.LectureType;

@Converter(autoApply = true)
public class LectureTypeConverter implements AttributeConverter<LectureType, String> {
    @Override
    public String convertToDatabaseColumn(LectureType lectureType) {
        return lectureType.toString();
    }

    @Override
    public LectureType convertToEntityAttribute(String s) {
        return LectureType.of(s);
    }
}
