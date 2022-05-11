package fit.wenchao.databasedatamock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 变量名转换
 * @Class: VariableNameConversion
 * @Author: Yiang37
 * @Date: 2020/11/3 15:23
 * @Version: 1.0
 */

public class VariableNameConversion {

    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    public static void main(String[] args) {

        System.out.println(lowerLineToHump("NAME_AND_addREsS"));

        System.out.println(capitalizeTheFirstLetter("nAMe"));

        System.out.println(humpToLowerLine("nameAndAddress"));

    }

    /**
     * @Description: 下划线格式 -> 驼峰 大小写均可
     * <p>
     * <p>
     * <p>
     * NAME_AND_ADDRESS -> nameAndAddress
     * @Author: Yiang37
     * @Date: 2020/11/03 15:26:02
     * @Version: 1.0
     * @method: lowerLineAndUppercaseToHump()
     * @param: [Big_]
     * @return: java.lang.String
     */

    public static String lowerLineToHump(String lowerLineAndUppercaseStr) {

//拆分成数组

        String[] eachStr = lowerLineAndUppercaseStr.split("_");

        StringBuilder resStr = new StringBuilder();

        String firstStr = "";

        String tempStr = "";

        for (int i = 0; i < eachStr.length; i++) {

//第一个数组全部小写

            if (i == 0) {

                firstStr = eachStr[0].toLowerCase();

                resStr.append(firstStr);

            } else {

//以后的数组首字母大写

                tempStr = capitalizeTheFirstLetter(eachStr[i]);

                resStr.append(tempStr);

            }

        }

        return resStr.toString();

    }

    /**
     * @Description: 任意字符串 -> 首字母大写
     * <p>
     * NAME -> Name
     * <p>
     * name -> Name
     * <p>
     * NaMe -> Name
     * @Author: Yiang37
     * @Date: 2020/11/03 16:50:16
     * @Version: 1.0
     * @method: capitalizeTheFirstLetter()
     * @param: [str]
     * @return: java.lang.String
     */

    public static String capitalizeTheFirstLetter(String str) {

        char firstChar = str.toUpperCase().charAt(0);

        String nextStr = str.toLowerCase().substring(1);

        return firstChar + nextStr;

    }

    /**
     * @Description: 驼峰 -> 下划线格式 默认小写,存在第二个形参且为true时大写.
     * @Author: Yiang37
     * @Date: 2020/11/03 17:10:25
     * @Version: 1.0
     * @method: humpToLowerLine()
     * @param: [humpStr, UppercaseZeroAndLowercaseOne]
     * @return: java.lang.String
     */

    public static String humpToLowerLine(String humpStr, boolean... defaultUppercaseAndTrueLowercase) {

        Matcher matcher = humpPattern.matcher(humpStr);

        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {

            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());

        }

        matcher.appendTail(sb);

//如果第二个形参为true 转为大写

        if (defaultUppercaseAndTrueLowercase.length >= 1 && defaultUppercaseAndTrueLowercase[0]) {

            return sb.toString().toUpperCase();

        }

        return sb.toString();

    }

}
