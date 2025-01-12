package pos.alexandruchi.academia.types.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pos.alexandruchi.academia.types.LectureCategory;

@Converter(autoApply = true)
public class LectureCategoryConverter implements AttributeConverter<LectureCategory, String> {
    @Override
    public String convertToDatabaseColumn(LectureCategory lectureCategory) {
        return lectureCategory.toString();
    }

    @Override
    public LectureCategory convertToEntityAttribute(String s) {
        return LectureCategory.of(s);
    }
}