package fit.wenchao.databasedatamock.mockMode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public interface MockMode {
    boolean supports(Annotation annotation);
    Object mockValue(Field targetField);
}