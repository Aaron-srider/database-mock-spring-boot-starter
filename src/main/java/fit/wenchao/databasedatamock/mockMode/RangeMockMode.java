package fit.wenchao.databasedatamock.mockMode;

import fit.wenchao.databasedatamock.annotation.*;
import fit.wenchao.databasedatamock.constant.MockModeEnum;
import fit.wenchao.databasedatamock.constant.StringCharsetEnum;
import fit.wenchao.utils.random.RandomUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Random;
import java.util.regex.Pattern;

public class RangeMockMode implements MockMode {

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
        return RandomUtils.randomIntRange(i1, i2);
    }

    public Object mockValue(Field targetField) {
        Class fieldType = targetField.getType();
        if (fieldType.equals(Integer.class)) {
            MockInt mockIntAnno = targetField.getAnnotation(MockInt.class);
            if (mockIntAnno == null) {
                throw new IllegalArgumentException(targetField.getName() + " is a Integer field with " +
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
                throw new IllegalArgumentException(targetField.getName() + " is a BigDecimal field with " +
                        "Range mode should be annotated with MockBigDecimal");
            }

            String decimalLength = mockBigDecimal.decimalLength();
            //String value = mockBigDecimal.value();
            String integerMax = mockBigDecimal.integerMax();
            String integerMin = mockBigDecimal.integerMin();

            //if(!Pattern.matches("-?([\\d*|0].\\d*)|(\\d*)", value)) {
            //    throw new IllegalArgumentException("value format error");
            //}

            if (!Pattern.matches("\\d*", decimalLength)) {
                throw new IllegalArgumentException("decimal format error");
            }

            if (!Pattern.matches("\\d*", integerMin)) {
                throw new IllegalArgumentException("integerMin format error");
            }

            if (!Pattern.matches("\\d*", integerMax)) {
                throw new IllegalArgumentException("integerMax format error");
            }

            BigDecimal decimalLen = new BigDecimal(decimalLength);
            BigDecimal minIntPart = new BigDecimal(integerMin);
            BigDecimal maxIntPart = new BigDecimal((integerMax));
            if (minIntPart.compareTo(maxIntPart) > 0) {
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
            if (maxIntPart.equals(minIntPart)) {
                intPartStr = minIntPart.toString();
            } else {
                int ran = RandomUtils.randomIntRange(minIntPart.intValue(), maxIntPart.intValue());
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
        } else if (fieldType.equals(String.class)) {
            MockString mockBigDecimal = targetField.getAnnotation(MockString.class);
            if (mockBigDecimal == null) {
                throw new IllegalArgumentException("String field with " +
                        "Range mode should be annotated with MockString");
            }

            MockStringTypeEnum type = mockBigDecimal.type();
            if(type.equals(MockStringTypeEnum.RANDOM_EACH_CHAR)) {
                int minlen = mockBigDecimal.minLen();
                int maxlen = mockBigDecimal.maxLen();
                if (minlen > maxlen) {
                    throw new IllegalArgumentException("minLen must not be greater " +
                            "than maxLen");
                }

                String prefix = mockBigDecimal.prefix();
                String suffix = mockBigDecimal.suffix();
                StringCharsetEnum stringCharsetEnum = mockBigDecimal.charset();
                if (stringCharsetEnum.equals(StringCharsetEnum.DIGITAL)) {
                    String s = prefix +  RandomUtils.randomStringFromDigitalFor(minlen, maxlen)
                            +suffix;
                    return s;
                } else if (stringCharsetEnum.equals(StringCharsetEnum.ALPHADIGITAL)) {
                    return prefix + RandomUtils.randomStringFromAlphaDigital(minlen, maxlen)+suffix;
                } else if (stringCharsetEnum.equals(StringCharsetEnum.ALPHABET)) {
                    return prefix + RandomUtils.randomStringFromAlphabetFor(minlen, maxlen)+suffix;
                } else if (stringCharsetEnum.equals(StringCharsetEnum.VISIBLEASCII)) {
                    String charset = "!#$%&()*+,-.0123456789:;<=>?ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_abcdefghijklmnopqrstuvwxyz{|}~";
                    return prefix + RandomUtils.randomFromCharset(minlen, maxlen, charset)+suffix;
                } else {
                    return prefix + RandomUtils.randomStringFromAlphaDigital(minlen, maxlen)+suffix;
                }
            } else if(type.equals(MockStringTypeEnum.CANDIDATES)) {
                String[] strings = mockBigDecimal.wordSet();
                if(strings.length == 0){
                    throw new IllegalArgumentException("Mock String with CANDIDATES type" +
                            " must provide candidate strings");
                }
                return strings[RandomUtils.randomIntRange(0, strings.length - 1)];
            }
            return null;

        } else {
            throw new IllegalArgumentException("Range Mode only" +
                    " support Integer and Boolean field");
        }

    }
}