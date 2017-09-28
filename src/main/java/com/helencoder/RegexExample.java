package com.helencoder;

import java.util.regex.Pattern;

/**
 * Created by helencoder on 2017/9/28.
 */
public class RegexExample {
    public static void main(String[] args) {
        // 正则表达式使用示例

        // 参考正则表达式
        String chineseCharacter = "[\\u4e00-\\u9fa5]";  // 单个中文字符
        String emailCharacter = "\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}";   // Email地址
        String urlCharacter = "^((https|http|ftp|rtsp|mms)?:\\/\\/)[^\\s]+";   // url地址
        String idCharacter = "\\d{17}[\\d|x]|\\d{15}";  // 身份证号
        String ipCharacter = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";  // ip地址

        boolean isMatch = Pattern.matches(ipCharacter, "");
        System.out.println(isMatch);
    }
}
