package fit.wenchao.databasedatamock.annotation;

import fit.wenchao.databasedatamock.constant.AppendEnum;
import fit.wenchao.databasedatamock.constant.StringCharsetEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MockString {

    /**
     * used for fixed mode
     * @return fixed String value
     */
    String value() default "";

    @Deprecated
    String base() default "";

    @Deprecated
    AppendEnum append() default AppendEnum.NONE;

    @Deprecated
    int appendLen() default 0;

    /**
     * used for range mode
     * @return min count of result string(inclusive)
     */
    int minLen() default 0;

    /**
     * used for range mode
     * @return max count of result string(inclusive)
     */
    int maxLen() default 0;

    /**
     * used for range mode, if not specified, char will be chosen from set of from the set of
     *  Latin alphabetic characters (a-z, A-Z) and the digits 0-9.
     * @return charset containing the set of characters to use
     */
    StringCharsetEnum charset() default StringCharsetEnum.ALPHADIGITAL;

    /**
     * used for range mode, pick word from set provided, if not specified, empty
     * array will be return. If wordSet is empty, null will be used.
     * @return a String array containing candidate words.
     */
    String[] wordSet() default {};


    MockStringTypeEnum type() default MockStringTypeEnum.RANDOM_EACH_CHAR;

    /**
     * used for range mode, if not specified, result has no prefix
     * @return the prefix of the mock result
     */
    String prefix() default "";

    /**
     * used for range mode, if not specified, result has no suffix
     * @return the suffix of the mock result
     */
    String suffix() default "";
}