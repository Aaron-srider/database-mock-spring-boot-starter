package fit.wenchao.databasedatamock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MockBigDecimal {

    /**
     * used for Fixed mode(developing)
     */
    //todo
    String value() default "0";

    /**
     * decimal part len, used for range mode
     */
    String decimalLength() default "0";

    /**
     * min value of int part, used for range mode
     */
    String integerMin() default "0";

    /**
     * max value of int part, used for range mode
     */
    String integerMax() default "0";
}
