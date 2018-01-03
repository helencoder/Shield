package com.helencoder.shield.util;

import java.util.List;

/**
 * 基础公共方法类
 *
 * Created by helencoder on 2018/1/3.
 */
public class BasicUtil {

    /**
     * 获取字符串出现次数
     *
     * @param str 字符串
     * @param pattern 子串
     * @return int 次数
     */
    public static int getOccurrences(String str, String pattern) {
        return KMP.getOccurrences(str, pattern);
    }

    /**
     * 字符串查找
     *
     * @param pattern 待查找的pattern
     * @param str     查找的字符串
     * @return boolean
     */
    public static boolean isPatternExists(String pattern, String str) {
        int index = KMP.search(str, pattern);
        return index != -1;
    }

    /**
     * 获取最长公共子序列
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return String 最长公共子序列
     */
    public static String getCommonSequence(String str1, String str2) {
        return LCS.commonSequence(str1, str2);
    }

    /**
     * 获取最长公共子串
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return String 最长公共子串
     */
    public static List<String> getCommonString(String str1, String str2) {
        return LCS.commonString(str1, str2);
    }

    /**
     * 获取拼音字符串
     *
     * @param str 字符串
     * @return pinyin
     */
    public static String getPinyin(String str) {
        return PinyinUtils.getPingYin(str);
    }

    /**
     * 获取拼音首字母字符串
     *
     * @param str 字符串
     * @return aleph
     */
    public static String getAleph(String str) {
        return PinyinUtils.getAleph(str);
    }

}
