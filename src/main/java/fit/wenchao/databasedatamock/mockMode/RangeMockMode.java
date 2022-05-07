package fit.wenchao.databasedatamock.mockMode;

import fit.wenchao.databasedatamock.annotation.MockBoolean;
import fit.wenchao.databasedatamock.annotation.MockField;
import fit.wenchao.databasedatamock.annotation.MockInt;
import fit.wenchao.databasedatamock.constant.MockModeEnum;
import fit.wenchao.databasedatamock.testPo.dao.po.MockBigDecimal;
import fit.wenchao.utils.random.RandomUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Random;
import java.util.regex.Pattern;

public class RangeMockMode implements MockMode {

        private MockField mockField;

        private boolean mockRangeBoolean(MockBoolean mockBoolean) {
            int i1 = new Random().nextInt(2);
            boolean newBoolean = (i1 != 0);

            return newBoolean;
        }

        public boolean supports(Annotation annotation) {
            if (annotation instanceof MockField) {
                return ((MockField) annotation).mode().equals(MockModeEnum.RANGE);
            }
            return false;
        }

        private int mockRangeInt(MockInt mockIntAnno) {
            int i1 = mockIntAnno.randomMin();
            int i2 = mockIntAnno.randomMax();
            if (i1 > i2) {
                throw new IllegalArgumentException("randomMax should" +
                        " be bigger than randomMin");
            }
            int newRandom = (new Random().nextInt(i2)) + i1;
            return newRandom;
        }

        public Object mockValue(Field targetField) {
            Class fieldType = targetField.getType();
            if (fieldType.equals(Integer.class)) {
                MockInt mockIntAnno = targetField.getAnnotation(MockInt.class);
                if (mockIntAnno == null) {
                    throw new IllegalArgumentException("Integer field with " +
                            "Range mode should be annotated with MockInt");
                }
                int newRandom = mockRangeInt(mockIntAnno);
                return newRandom;
            } else if (fieldType.equals(Boolean.class)) {
                MockBoolean mockBoolean = targetField.getAnnotation(MockBoolean.class);
                if (mockBoolean == null) {
                    throw new IllegalArgumentException("Boolean field with " +
                            "Range mode should be annotated with MockBoolean");
                }
                boolean newBoolean = mockRangeBoolean(mockBoolean);
                return newBoolean;
            } else if (fieldType.equals(BigDecimal.class)) {
                MockBigDecimal mockBigDecimal = targetField.getAnnotation(MockBigDecimal.class);
                if (mockBigDecimal == null) {
                    throw new IllegalArgumentException("BigDecimal field with " +
                            "Range mode should be annotated with MockBigDecimal");
                }

                String decimal = mockBigDecimal.decimal();
                //String value = mockBigDecimal.value();
                String integerMax = mockBigDecimal.integerMax();
                String integerMin = mockBigDecimal.integerMin();

                //if(!Pattern.matches("-?([\\d*|0].\\d*)|(\\d*)", value)) {
                //    throw new IllegalArgumentException("value format error");
                //}

                if (!Pattern.matches("\\d*", decimal)) {
                    throw new IllegalArgumentException("decimal format error");
                }

                if (!Pattern.matches("\\d*", integerMin)) {
                    throw new IllegalArgumentException("integerMin format error");
                }

                if (!Pattern.matches("\\d*", integerMax)) {
                    throw new IllegalArgumentException("integerMax format error");
                }

                BigDecimal decimalLen = new BigDecimal(decimal);
                BigDecimal bigDecimal = new BigDecimal(integerMin);

                BigDecimal bigDecimal1 = new BigDecimal((integerMax));
                if (bigDecimal.compareTo(bigDecimal1) > 0) {
                    throw new IllegalArgumentException("integerMin bigger" +
                            " than integerMax");
                }

                if (decimalLen.compareTo(BigDecimal.valueOf(0)) < 0) {
                    throw new IllegalArgumentException("decimal must be bigger" +
                            " than 0");
                }

                StringBuilder decimalPart = new StringBuilder();
                for (BigDecimal i = new BigDecimal(0); i.compareTo(decimalLen) < 0; i = i.add(BigDecimal.valueOf(1))) {
                    int ran = RandomUtils.randomIntRange(0, 9);
                    decimalPart.append(ran);
                }

                String decimalPartStr = decimalPart.toString();
                String intPartStr;
                if (bigDecimal1.equals(bigDecimal)) {
                    intPartStr = bigDecimal.toString();
                } else {
                    int ran = RandomUtils.randomIntRange(bigDecimal.intValue(), bigDecimal1.intValue());
                    intPartStr = String.valueOf(ran);
                }
                String decimalWhole;
                if (!decimalPartStr.equals("")) {
                    decimalWhole = intPartStr + "." + decimalPartStr;
                } else {
                    decimalWhole = intPartStr;
                }
                BigDecimal bigDecimal2 = new BigDecimal(decimalWhole);
                return bigDecimal2;
            } else {
                throw new IllegalArgumentException("Range Mode only" +
                        " support Integer and Boolean field");
            }
        }
    }