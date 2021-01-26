package com.beyond.mybatisplus.batch.proxy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author beyond
 */
public class StringUtils {
    private static final String UNDERLINE_STR = "_";
    private static final char UNDERLINE_CHAR = '_';

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
            matcher.appendReplacement(sb, UNDERLINE_STR + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        if (sb.toString().startsWith(UNDERLINE_STR)){
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


    /**
     * 下划线转驼峰
     */
    private String lineToHump(String line) {
        if (line == null) {
            return null;
        }
        if ("".equals(line.trim())) {
            return line;
        }
        int len = line.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = line.charAt(i);
            if (c == UNDERLINE_CHAR) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(line.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
