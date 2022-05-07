package fit.wenchao.databasedatamock.mockMode;

import java.lang.annotation.Annotation;

public interface CustomMockFieldAnnotationProvider {
    Class<? extends Annotation> getAnnotation();
}