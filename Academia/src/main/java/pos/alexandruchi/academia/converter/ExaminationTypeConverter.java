package pos.alexandruchi.academia.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pos.alexandruchi.academia.converter.types.*;

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
