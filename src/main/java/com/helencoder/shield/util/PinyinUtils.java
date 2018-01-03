package com.helencoder.shield.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Java中文转换为拼音工具类
 *
 * Created by helencoder on 2018/1/3.
 */
class PinyinUtils {

    /**
     * 获得汉语拼音首字母,英文字符不进行控制
     *
     * @param str 字符串
     * @return pinyin
     */
    static String getAleph(String str) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        StringBuilder sb = new StringBuilder();
        if (str != null && str.length() > 0 && !"null".equals(str)) {
            char[] charArray = str.trim().toCharArray();
            try {
                for (char c : charArray) {
                    if (String.valueOf(c).matches("[\\u4E00-\\u9FA5]")) {   // 中文字符匹配
                        sb.append(PinyinHelper.toHanyuPinyinStringArray(c, format)[0].charAt(0));
                    } else {
                        sb.append(String.valueOf(c));
                    }
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        } else {
            return "*";
        }
        return sb.toString();
    }

    /**
     * 将字符串中的中文转化为拼音,英文字符不变
     *
     * @param str 字符串
     * @return pinyin
     */
    static String getPingYin(String str) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        StringBuilder sb = new StringBuilder();
        if (str != null && str.length() > 0 && !"null".equals(str)) {
            char[] charArray = str.trim().toCharArray();
            try {
                for (char c : charArray) {
                    if (String.valueOf(c).matches("[\\u4E00-\\u9FA5]")) {   // 中文字符匹配
                        sb.append(PinyinHelper.toHanyuPinyinStringArray(c, format)[0]);
                    } else {
                        sb.append(String.valueOf(c));
                    }
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        } else {
            return "*";
        }
        return sb.toString();
    }

}
