package com.thirdparty.morph.identification.feature;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;


/**
 * Created by helencoder on 2018/1/4.
 */
public class Pinyin {
    private HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();

    public static void main(String[] args) throws Exception {
        String str = "外焦布";
        Pinyin p = new Pinyin();
        p.pinyinword(str);
    }

    public Pinyin() throws Exception {
    }

    public ArrayList<String> pinyinword(String word) throws Exception {
        if(word.length() >= 7) {
            return null;
        } else {
            ArrayList<ArrayList<String>> arr = new ArrayList();

            ArrayList flist;
            for(int i = 0; i < word.length(); ++i) {
                char ch = word.charAt(i);
                flist = new ArrayList();
                String[] pinyinArray = this.pinyincharacter(ch);
                if(pinyinArray != null) {
                    for(int j = 0; j < pinyinArray.length; ++j) {
                        flist.add(pinyinArray[j]);
                    }
                }

                arr.add(flist);
            }

            if(arr.size() <= 0) {
                return null;
            } else {
                ArrayList<String> prev_list = new ArrayList();
                ArrayList<String> curr_list = new ArrayList();
                flist = (ArrayList)arr.get(0);

                int i;
                for(i = 0; i < flist.size(); ++i) {
                    prev_list.add((String)flist.get(i));
                }

                for(i = 1; i < arr.size(); ++i) {
                    ArrayList<String> list = (ArrayList)arr.get(i);

                    for(int k = 0; k < list.size(); ++k) {
                        String pinyin = (String)list.get(k);

                        for(int j = 0; j < prev_list.size(); ++j) {
                            curr_list.add((String)prev_list.get(j) + " " + pinyin);
                        }
                    }

                    prev_list = curr_list;
                    curr_list = new ArrayList();
                }

                for(i = 0; i < prev_list.size(); ++i) {
                    ;
                }

                return prev_list;
            }
        }
    }

    public String[] pinyincharacter(char ch) throws Exception {
        try {
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(ch, this.defaultFormat);
            return pinyinArray;
        } catch (BadHanyuPinyinOutputFormatCombination var3) {
            return null;
        }
    }
}
