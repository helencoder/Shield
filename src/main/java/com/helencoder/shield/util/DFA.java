package com.helencoder.shield.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * DFA(Deterministic Finite Automaton)算法
 *
 * Created by helencoder on 2018/1/5.
 */
public class DFA {
    private String dictPath;
    private Set sensitiveWordsSet;
    private Map sensitiveWordsMap;

    public DFA(String dictPath) {
        this.dictPath = dictPath;
        this.sensitiveWordsSet = new HashSet<String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dictPath)));
            for (String str = br.readLine(); str != null; str = br.readLine()) {
                this.sensitiveWordsSet.add(str.trim());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 敏感词map构建
     */
    public void init() {
        this.sensitiveWordsMap = new HashMap<String, String>(this.sensitiveWordsSet.size());
        Set<String> sensitiveWordsSet = this.sensitiveWordsSet;
        for (String word : sensitiveWordsSet) {
            Map nowMap = this.sensitiveWordsMap;
            for (int i = 0; i < word.length(); i++) {
                // 转换成char型
                char keyChar = word.charAt(i);
                // 获取
                Object tempMap = nowMap.get(keyChar);

                if (tempMap != null) {  // 如果存在该key，直接赋值
                    nowMap = (Map) tempMap;
                } else { // 不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个

                    // 设置标志位
                    Map<String, String> newMap = new HashMap<String, String>();
                    newMap.put("isEnd", "0");

                    // 添加到集合
                    nowMap.put(keyChar, newMap);
                    nowMap = newMap;
                }

                // 最后一个
                if (i == word.length() - 1) {
                    nowMap.put("isEnd", "1");
                }
            }
        }
    }

    /**
     * 敏感词检测
     */
    public boolean check(String str) {
        boolean  flag = false;    //敏感词结束标识位：用于敏感词只有1位的情况
        char word = 0;
        Map nowMap = this.sensitiveWordsMap;
        for(int i = 0; i < str.length() ; i++){
            word = str.charAt(i);
            nowMap = (Map) nowMap.get(word);     //获取指定key
            if(nowMap != null){     //存在，则判断是否为最后一个
                if("1".equals(nowMap.get("isEnd"))){       //如果为最后一个匹配规则,结束循环，返回匹配标识数
                    flag = true;       //结束标志位为true
                }
            } else{     //不存在，直接返回
                break;
            }
        }

        return flag;
    }

}
