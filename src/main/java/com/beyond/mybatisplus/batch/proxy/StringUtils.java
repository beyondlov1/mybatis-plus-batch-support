package com.beyond.mybatisplus.batch.proxy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author beyond
 */
public class StringUtils {
    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    /**
     * 驼峰转下划线
     * @param str 驼峰字符串
     * @return 下划线字符串
     */
    public static String humpToLine(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        if (sb.toString().startsWith("_")){
            return sb.substring(1, sb.length());
        }
        return sb.toString();
    }

    /**
     * 首字母变小写
     * @param name 源
     * @return 结果
     */
    public static String deCapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
