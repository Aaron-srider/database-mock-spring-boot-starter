package fit.wenchao.databasedatamock.mockMode;

import fit.wenchao.databasedatamock.annotation.MockBoolean;
import fit.wenchao.databasedatamock.annotation.MockField;
import fit.wenchao.databasedatamock.annotation.MockInt;
import fit.wenchao.databasedatamock.annotation.MockString;
import fit.wenchao.databasedatamock.constant.MockModeEnum;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class FixedMockMode implements MockMode {

    public boolean supports(Annotation annotation) {
        if (annotation instanceof MockField) {
            return ((MockField) annotation).mode().equals(MockModeEnum.FIXED);
        }
        return false;
    }

    public Object mockValue(Field targetField) {
        Class fieldType = targetField.getType();
        if (fieldType.equals(Integer.class)) {
            MockInt annotation2 = targetField.getAnnotation(MockInt.class);
            if (annotation2 == null) {
                throw new IllegalArgumentException("Integer " +
                        "field should be annotated with MockInt");
            }

            int value = annotation2.value();
            return value;
        } else if (fieldType.equals(Boolean.class)) {
            MockBoolean annotation2 = targetField.getAnnotation(MockBoolean.class);
            if (annotation2 == null) {
                throw new IllegalArgumentException("Boolean " +
                        "field should be annotated with MockBoolean");
            }
            boolean value = annotation2.value();
            return value;
        } else if (fieldType.equals(String.class)) {
            MockString annotation2 = targetField.getAnnotation(MockString.class);
            if (annotation2 == null) {
                throw new IllegalArgumentException("String " +
                        "field should be annotated with MockString");
            }
            String value = annotation2.value();
            return value;
        } else {
            throw new IllegalArgumentException("Fixed Mode only" +
                    " support Integer and Boolean field");
        }
    }
}