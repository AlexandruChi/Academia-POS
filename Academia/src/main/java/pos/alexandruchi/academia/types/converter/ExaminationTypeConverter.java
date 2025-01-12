package pos.alexandruchi.academia.types.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pos.alexandruchi.academia.types.ExaminationType;

@Converter(autoApply = true)
public class ExaminationTypeConverter implements AttributeConverter<ExaminationType, String> {
    @Override
    public String convertToDatabaseColumn(ExaminationType examinationType) {
        return examinationType.toString();
    }

    @Override
    public ExaminationType convertToEntityAttribute(String s) {
        return ExaminationType.of(s);
    }
}
