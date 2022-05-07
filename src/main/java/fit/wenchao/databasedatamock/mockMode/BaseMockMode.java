package fit.wenchao.databasedatamock.mockMode;

import fit.wenchao.databasedatamock.annotation.MockField;
import fit.wenchao.databasedatamock.annotation.MockString;
import fit.wenchao.databasedatamock.constant.AppendEnum;
import fit.wenchao.databasedatamock.constant.MockModeEnum;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static fit.wenchao.databasedatamock.NanoIdUtils.randomNanoId;

public class BaseMockMode implements MockMode {

        private MockField mockField;

        public boolean supports(Annotation annotation) {
            if (annotation instanceof MockField) {
                return ((MockField) annotation).mode().equals(MockModeEnum.BASE);
            }
            return false;
        }

        public Object mockValue(Field targetField) {
            MockString mockStringAnno = null;
            Class fieldType = targetField.getType();
            if (!fieldType.equals(String.class) || (mockStringAnno = targetField.getAnnotation(MockString.class)) == null) {
                throw new IllegalArgumentException("MockField mode 'BASE'," +
                        " only support String.class and should be used along " +
                        "with MockString");
            }

            String base;
            AppendEnum appendStrategy = AppendEnum.NONE;
            int appendLen;
            base = mockStringAnno.base();
            appendStrategy = mockStringAnno.append();
            appendLen = mockStringAnno.appendLen();
            if (appendStrategy.equals(AppendEnum.NANOID)) {
                return base + randomNanoId(appendLen);
            } else {
                throw new IllegalArgumentException("Append only support AppendEnum.NANOID" +
                        " for now");
            }
        }
    }