package fit.wenchao.databasedatamock.annotation;

import sun.text.resources.iw.FormatData_iw_IL;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MockInt {
    int value() default 0;

    int randomMin() default 0;

    int randomMax() default Integer.MAX_VALUE;
}
