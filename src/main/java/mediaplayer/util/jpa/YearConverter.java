package mediaplayer.util.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Year;

@Converter
public class YearConverter implements AttributeConverter<Year, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Year year) {
        return year == null ? 0 : year.getValue();
    }

    @Override
    public Year convertToEntityAttribute(Integer value) {
        return Year.of(value);
    }

}
