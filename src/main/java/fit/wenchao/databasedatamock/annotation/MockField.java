package fit.wenchao.databasedatamock.annotation;

import fit.wenchao.databasedatamock.constant.MockModeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MockField {

    MockModeEnum mode();


}
