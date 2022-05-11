package fit.wenchao.databasedatamock.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MockRow {
    /**
     * required
     */
    int value();

    boolean delBeforeMock() default false;
}
