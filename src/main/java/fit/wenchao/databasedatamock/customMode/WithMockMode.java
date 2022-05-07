package fit.wenchao.databasedatamock.customMode;

import fit.wenchao.databasedatamock.mockMode.MockMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithMockMode {
    Class<? extends MockMode> clazz();
}
