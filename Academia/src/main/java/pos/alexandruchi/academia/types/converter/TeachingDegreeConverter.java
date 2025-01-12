package pos.alexandruchi.academia.types.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pos.alexandruchi.academia.types.TeachingDegree;

@Converter(autoApply = true)
public class TeachingDegreeConverter implements AttributeConverter<TeachingDegree, String> {
    @Override
    public String convertToDatabaseColumn(TeachingDegree teachingDegree) {
        return teachingDegree.toString();
    }

    @Override
    public TeachingDegree convertToEntityAttribute(String s) {
        return TeachingDegree.of(s);
    }
}
