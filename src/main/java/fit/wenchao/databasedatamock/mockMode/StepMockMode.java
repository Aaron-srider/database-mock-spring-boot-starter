package fit.wenchao.databasedatamock.mockMode;

import fit.wenchao.databasedatamock.annotation.MockField;
import fit.wenchao.databasedatamock.annotation.MockInt;
import fit.wenchao.databasedatamock.constant.MockModeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@Data
@AllArgsConstructor
public class StepMockMode implements MockMode {

    private static Integer currentStepCount = 0;
    //private Integer totalStep;

    public boolean supports(Annotation annotation) {
        if (annotation instanceof MockField) {
            return ((MockField) annotation).mode().equals(MockModeEnum.STEP);
        }
        return false;
    }

    public Object mockValue(Field targetField) {
        Class fieldType = targetField.getType();
        if (fieldType.equals(Integer.class)) {
            MockInt annotation2 = targetField.getAnnotation(MockInt.class);
            if (annotation2 == null) {
                throw new IllegalArgumentException("Integer " +
                        "field should be annotated with MockInt");
            }

            int steplen = annotation2.stepLen();

            int baseStep = annotation2.baseStep();

            int result = baseStep + steplen * getCurrentStepCount();

            setCurrentStepCount(getCurrentStepCount() + 1);
            return result;
        } else {
            throw new IllegalArgumentException("Fixed Mode only" +
                    " support Integer and Boolean field");
        }
    }

    private static void setCurrentStepCount(int i) {
        currentStepCount  = i;
    }

    private static int getCurrentStepCount() {
        return currentStepCount;
    }
}