package fit.wenchao.databasedatamock.customMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@WithMockMode(clazz=FixedValueMockMode.class)
public @interface FixedIntValue {
    int value();
}