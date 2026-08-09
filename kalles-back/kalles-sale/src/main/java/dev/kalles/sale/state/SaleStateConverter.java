package dev.kalles.sale.state;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SaleStateConverter implements AttributeConverter<SaleState, String> {

    @Override
    public String convertToDatabaseColumn(SaleState state) {
        if (state == null) {
            return OpenState.NAME; 
        }
        return state.getName();
    }

    @Override
    public SaleState convertToEntityAttribute(String stateName) {
        return SaleStateFactory.fromName(stateName);
    }
}
