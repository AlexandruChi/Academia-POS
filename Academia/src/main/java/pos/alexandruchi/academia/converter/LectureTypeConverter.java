package pos.alexandruchi.academia.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pos.alexandruchi.academia.converter.types.*;

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
