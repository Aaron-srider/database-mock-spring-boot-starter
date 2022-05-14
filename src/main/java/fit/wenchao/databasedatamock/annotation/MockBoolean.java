package fit.wenchao.databasedatamock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MockBoolean {
    /**
     * used for fixed mode
     * @return fixed value of MockBoolean
     */
    boolean value() default  false;
}
