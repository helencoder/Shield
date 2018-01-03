package com.helencoder.shield.util;

import java.util.regex.Pattern;

/**
 * 正则表达式匹配样例
 *
 * Created by helencoder on 2018/1/3.
 */
public class RegrexPattern {
    private static final String chineseCharacter = "[\\u4e00-\\u9fa5]";  // 单个中文字符
    private static final String emailCharacter = "\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}";   // Email地址
    private static final String urlCharacter = "^((https|http|ftp|rtsp|mms)?:\\/\\/)[^\\s]+";   // url地址
    private static final String idCharacter = "\\d{17}[\\d|x]|\\d{15}";  // 身份证号
    private static final String ipCharacter = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";  // ip地址

    public static boolean isMatch(String str, String flag) {
        String pattern;
        switch (flag) {
            case "chinese":
                pattern = chineseCharacter;
                break;
            case "email":
                pattern = emailCharacter;
                break;
            case "url":
                pattern = urlCharacter;
                break;
            case "id":
                pattern = idCharacter;
                break;
            case "ip":
                pattern = ipCharacter;
                break;
            default:
                pattern = chineseCharacter;
                break;
        }
        return Pattern.matches(pattern, str);
    }

}
