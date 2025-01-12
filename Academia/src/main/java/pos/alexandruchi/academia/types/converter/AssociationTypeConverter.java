package pos.alexandruchi.academia.types.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pos.alexandruchi.academia.types.AssociationType;

@Converter(autoApply = true)
public class AssociationTypeConverter implements AttributeConverter<AssociationType, String> {
    @Override
    public String convertToDatabaseColumn(AssociationType associationType) {
        return associationType.toString();
    }

    @Override
    public AssociationType convertToEntityAttribute(String s) {
        return AssociationType.of(s);
    }
}
