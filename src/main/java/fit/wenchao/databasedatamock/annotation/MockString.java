package fit.wenchao.databasedatamock.annotation;

import fit.wenchao.databasedatamock.constant.AppendEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MockString {
    String base() default "";
    String value() default "";
    AppendEnum append() default AppendEnum.NONE;
    int appendLen() default 0;
}
