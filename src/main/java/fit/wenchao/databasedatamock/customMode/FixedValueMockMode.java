package fit.wenchao.databasedatamock.customMode;

import fit.wenchao.databasedatamock.mockMode.MockMode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class FixedValueMockMode implements MockMode {

    @Override
    public boolean supports(Annotation annotation) {
        return annotation instanceof FixedIntValue;
    }

    @Override
    public Object mockValue(Field targetField) {
        FixedIntValue annotation = targetField.getAnnotation(FixedIntValue.class);
        if (annotation != null) {
            int value = annotation.value();
            return value;
        }
        throw new IllegalArgumentException("FixedIntValue value not found");
    }
}