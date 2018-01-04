package com.thirdparty.morph.identification;

import com.thirdparty.morph.identification.document.*;
import com.thirdparty.morph.identification.feature.Pinyin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 特征提取
 *
 * Created by helencoder on 2018/1/4.
 */
public class FeatureGeneration {
    private HashMap<String, HashSet<String>> anns = new HashMap<>();
    private String train_path;
    private String test_path;
    private HashSet<String> train_ids;
    private HashSet<String> test_ids;
    private HashMap<String, HashMap<String, Integer>> mention_train;
    private HashMap<String, HashMap<String, Integer>> mention_test;
    private HashMap<String, Integer> mention_freq_train;
    private HashMap<String, Integer> mention_freq_test;
    private HashMap<String, Integer> men2tid;
    private HashMap<String, Integer> pos2tid;
    private HashMap<Integer, HashMap<String, Integer>> lex2tid;
    private Dictionary first_name;
    private Dictionary last_name;
    private Dictionary per_name;
    private Dictionary per_suffix;
    private Dictionary gpe_name;
    private Dictionary gpe_suffix;
    private Dictionary fac_name;
    private Dictionary nationality;
    private Dictionary nom_list;
    private Dictionary org_name;
    private Dictionary org_suffix;
    private Dictionary stop_name;
    private Dictionary dict;
    private MentionSet mset_train;
    private MentionSet mset_test;
    private TweetNerSet ner_set;
    private HashMap<String, String> ners;
    private Pinyin pinyin;
    private HashMap<String, HashSet<String>> pinyin_dict;

    public static void main(String[] args) throws Exception {
        String test_path = "./data/exp/tweets/test";
        String test_context_path = "./data/exp/tweets/preprocess.txt";

        String ftest_path = "./data/exp/discovery/test.feature";
        String ftest_path2 = "./data/exp/discovery/test.mention";

        String train_path = "./data/exp/tweets/train";
        String annotation_path = "./data/exp/annotation/morph_list_05_05";
        FeatureGeneration fg = new FeatureGeneration(train_path, test_path, annotation_path, test_context_path);
        fg.generate_features(ftest_path, ftest_path2);
    }

    public FeatureGeneration(String train_path, String test_path, String annotation_path, String test_context_path) throws Exception {
        this.test_path = test_path;
        this.train_path = train_path;
        this.train_ids = new HashSet<>();
        this.test_ids = new HashSet<>();
        this.mset_train = new MentionSet();
        this.mset_test = new MentionSet();
        this.ner_set = new TweetNerSet();
        this.ners = new HashMap<>();
        this.read_tids(train_path, this.train_ids);
        this.read_tids(test_path, this.test_ids);
        this.mention_train = new HashMap<>();
        this.mention_test = new HashMap<>();
        this.mention_freq_train = new HashMap<>();
        this.mention_freq_test = new HashMap<>();
        this.men2tid = new HashMap<>();
        this.pos2tid = new HashMap<>();
        this.lex2tid = new HashMap<>();
        this.read_annotation(annotation_path);
        String pos_path = "./data/exp/tweets/preprocess.txt";
        this.read_mentions(pos_path);
        this.read_mentions(test_context_path);
        this.init_dict();
        System.out.println("finished reading dicts");
        String ner_path = "./data/exp/tweets/ner.txt";
        this.read_ners(ner_path);
        this.construct_tweet_ner_set(pos_path);
        this.pinyin = new Pinyin();
        this.pinyin_dict = new HashMap<>();
        String pinyin_dict_path = "./data/pinyin.dict";
        this.read_pinyin_dict(pinyin_dict_path);
    }

    public void generate_features(String ftest_path, String ftest_path2) throws Exception {
        this.generate_features(this.mention_test, this.mset_test, this.mention_freq_test, ftest_path, ftest_path2);
    }

    public void generate_features(HashMap<String, HashMap<String, Integer>> mentions, MentionSet mset, HashMap<String, Integer> map_freq, String outputpath, String outputpath2) throws Exception {
        int mlen = this.men2tid.size();
        int plen = this.pos2tid.size();
        int lex_len = 0;
        Iterator it2 = this.lex2tid.keySet().iterator();

        while(it2.hasNext()) {
            Integer n = (Integer)it2.next();
            HashMap<String, Integer> hmap = (HashMap)this.lex2tid.get(n);
            if(n.intValue() != 4) {
                lex_len += hmap.size();
            }
        }

        File output_file = new File(outputpath);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_file), "UTF-8"));
        File output_file2 = new File(outputpath2);
        BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_file2), "UTF-8"));
        Iterator it = mentions.keySet().iterator();

        while(it.hasNext()) {
            String m = (String)it.next();
            String label = "0";
            if(this.anns.containsKey(m)) {
                label = "1";
            }

            bw.write(label);
            String fword = this.word(m);
            if(fword != null) {
                bw.write(" " + fword);
            }

            String flexical = this.lexical(m, this.lex2tid, mlen);
            if(flexical != null) {
                bw.write(" " + flexical);
            }

            HashMap<String, Integer> pos_map = (HashMap)mentions.get(m);
            String fpos = this.pos(pos_map, mlen + lex_len);
            if(fpos != null) {
                bw.write(" " + fpos);
            }

            int fd0 = mlen + plen + lex_len;
            bw.write(" " + this.wlen(m, fd0));
            int fd1 = fd0 + 1;
            bw.write(" " + this.isInDictionary(m, fd1));
            bw.write(" " + this.isNorm(m, fd1));
            int fd3 = fd1 + 1;
            bw.write(" " + this.fperson(m, fd3));
            int fd4 = fd3 + 1;
            bw.write(" " + this.fgpe(m, fd4));
            int fd5 = fd4 + 1;
            bw.write(" " + this.forg(m, fd5));
            int fd6 = fd5 + 1;
            bw.write(" " + this.f_pinyin(m, fd6) + "\n");
            bw2.write(m + "\t" + label + "\n");
        }

        bw.close();
        bw2.close();
    }

    private String word(String m) {
        Integer n = (Integer)this.men2tid.get(m);
        return n != null?n + ":" + 1:null;
    }

    private String lexical(String m, HashMap<Integer, HashMap<String, Integer>> hmap, int fd) {
        HashMap<String, Integer> map_1 = (HashMap)hmap.get(Integer.valueOf(1));
        HashMap<String, Integer> map_2 = (HashMap)hmap.get(Integer.valueOf(2));
        HashMap<String, Integer> map_3 = (HashMap)hmap.get(Integer.valueOf(3));
        HashMap<String, Integer> map_4 = (HashMap)hmap.get(Integer.valueOf(4));
        int fd2 = fd + map_1.size();
        int fd3 = fd2 + map_2.size();
        int var10000 = fd3 + map_3.size();
        int n = 3;
        if(m.length() < n) {
            int var17 = m.length();
        }

        StringBuffer sb = new StringBuffer();
        int mlen = m.length();
        String str;
        Integer index3;
        if(mlen >= 1) {
            str = m.substring(0, 1);
            index3 = (Integer)map_1.get(str);
            if(index3 != null) {
                sb.append(fd + index3.intValue() + ":" + 1 + " ");
            }
        }

        if(mlen >= 2) {
            str = m.substring(0, 2);
            index3 = (Integer)map_2.get(str);
            if(index3 != null) {
                sb.append(fd2 + index3.intValue() + ":" + 1 + " ");
            }
        }

        if(mlen >= 3) {
            str = m.substring(0, 3);
            index3 = (Integer)map_3.get(str);
            if(index3 != null) {
                sb.append(fd3 + index3.intValue() + ":" + 1 + " ");
            }
        }

        if(mlen >= 1) {
            str = m.substring(m.length() - 1, m.length());
            index3 = (Integer)map_4.get(str);
        }

        str = sb.toString().trim();
        return str.length() > 1?str:null;
    }

    private String pos(HashMap<String, Integer> pos_map, int mlen) {
        String pos = null;
        int max = -1;
        Iterator it = pos_map.keySet().iterator();

        while(it.hasNext()) {
            String item = (String)it.next();
            Integer n = (Integer)pos_map.get(item);
            if(n.intValue() > max) {
                max = n.intValue();
                pos = item;
            }
        }

        Integer n = (Integer)this.pos2tid.get(pos);
        if(n != null) {
            return mlen + n.intValue() + ":" + 1;
        } else {
            return null;
        }
    }

    private String wlen(String m, int fd) {
        return fd + ":" + m.length();
    }

    private String isInDictionary(String m, int fd) {
        boolean b1 = this.per_name.contains(m);
        boolean b2 = this.gpe_name.contains(m);
        boolean b3 = this.org_name.contains(m);
        boolean b4 = this.fac_name.contains(m);
        boolean b5 = this.nationality.contains(m);
        return !b1 && !b2 && !b3 && !b4 && !b5?fd + ":" + 0:fd + ":" + 1;
    }

    private String isNorm(String m, int fd) {
        boolean b = this.nom_list.contains(m);
        return b?fd + ":" + 1:fd + ":" + 0;
    }

    private String fperson(String m, int fd) {
        String last = m.substring(0, 1);
        boolean b = this.last_name.contains(last);
        if(b) {
            boolean b2 = true;

            for(int i = 1; i < m.length(); ++i) {
                String ch = m.substring(i, i + 1);
                if(!this.first_name.contains(ch)) {
                    b2 = false;
                }
            }

            if(!b2) {
                return fd + ":" + 1;
            } else {
                return fd + ":" + 0;
            }
        } else {
            return fd + ":" + 0;
        }
    }

    private String fgpe(String m, int fd) {
        if(m.length() == 1) {
            return fd + ":" + 0;
        } else {
            for(int i = m.length() - 1; i >= 0; --i) {
                String suffix = m.substring(i);
                if(this.gpe_suffix.contains(suffix)) {
                    if(!this.gpe_name.contains(m) && !this.dict.contains(m)) {
                        return fd + ":" + 1;
                    }

                    return fd + ":" + 0;
                }
            }

            return fd + ":" + 0;
        }
    }

    private String forg(String m, int fd) {
        for(int i = m.length() - 1; i >= 0; --i) {
            String suffix = m.substring(i);
            if(this.org_suffix.contains(suffix)) {
                if(this.org_name.contains(m)) {
                    return fd + ":" + 0;
                }

                return fd + ":" + 1;
            }
        }

        return fd + ":" + 0;
    }

    public boolean isChinese(String str) {
        char[] charArray = str.toCharArray();

        int i;
        for(i = 0; i < charArray.length; ++i) {
            UnicodeBlock ub = UnicodeBlock.of(charArray[i]);
            if(ub == UnicodeBlock.KATAKANA || ub == UnicodeBlock.HIRAGANA) {
                return false;
            }
        }

        for(i = 0; i < charArray.length; ++i) {
            if(charArray[i] < 19968 || charArray[i] > '龥') {
                return false;
            }
        }

        return true;
    }

    public String f_pinyin(String m, int fd) throws Exception {
        ArrayList<String> list = this.pinyin.pinyinword(m);
        if(list != null) {
            for(int i = 0; i < list.size(); ++i) {
                String w_pinyin = (String)list.get(i);
                HashSet<String> hset = (HashSet)this.pinyin_dict.get(w_pinyin);
                if(hset != null && !hset.contains(m) && !this.dict.contains(m)) {
                    return fd + ":" + 1;
                }
            }
        }

        return fd + ":" + 0;
    }

    public void read_coccur(String inputpath, HashMap<String, HashMap<String, Integer>> hmap) throws Exception {
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;

        while((str = br.readLine()) != null) {
            str = str.trim();
            String[] items = str.split("\t");
            String mention = items[0].trim();
            HashMap<String, Integer> hmap2 = new HashMap();

            for(int i = 1; i < items.length; ++i) {
                String item = items[i].trim();
                String[] tokens = item.split("\\$\\$");
                String word = tokens[0].trim();

                try {
                    if(word.length() > 1) {
                        Integer n = new Integer(tokens[1].trim());
                        hmap2.put(word, n);
                    }
                } catch (Exception var14) {
                    ;
                }
            }

            hmap.put(mention, hmap2);
        }

        br.close();
    }

    public void read_mentions(String inputpath) throws Exception {
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;

        while(true) {
            while((str = br.readLine()) != null) {
                str = str.trim();
                String[] items = str.split("\t");
                String tid = items[0].trim();
                int i;
                String[] wpos;
                String word;
                String pos;
                Integer n2;
                Mention men;
                Integer n;
                HashMap hmap;
                if(this.train_ids.contains(tid)) {
                    for(i = 1; i < items.length; ++i) {
                        wpos = items[i].trim().split("\\|");
                        word = wpos[0].trim();
                        pos = wpos[1].trim();
                        n2 = (Integer)this.mention_freq_train.get(word);
                        if(n2 == null) {
                            n2 = Integer.valueOf(0);
                        }

                        this.mention_freq_train.put(word, Integer.valueOf(n2.intValue() + 1));
                        if(this.mset_train.isExists(word)) {
                            men = this.mset_train.getMention(word);
                            men.addTid(tid);
                        } else {
                            men = new Mention(word);
                            men.addTid(tid);
                            this.mset_train.addMention(word, men);
                        }

                        hmap = (HashMap)this.mention_train.get(word);
                        if(hmap == null) {
                            hmap = new HashMap();
                        }

                        n = (Integer)hmap.get(pos);
                        if(n == null) {
                            n = Integer.valueOf(0);
                        }

                        hmap.put(pos, Integer.valueOf(n.intValue() + 1));
                        this.mention_train.put(word, hmap);
                        Integer id = (Integer)this.men2tid.get(word);
                        if(id == null) {
                            this.men2tid.put(word, Integer.valueOf(this.men2tid.size()));
                        }

                        Integer pid = (Integer)this.pos2tid.get(pos);
                        if(pid == null) {
                            this.pos2tid.put(pos, Integer.valueOf(this.pos2tid.size()));
                        }

                        int n3 = 3;
                        if(word.length() < 3) {
                            n3 = word.length();
                        }

                        HashMap lex_map;
                        for(int k = 1; k <= n3; ++k) {
                            lex_map = (HashMap)this.lex2tid.get(new Integer(k));
                            if(lex_map == null) {
                                lex_map = new HashMap();
                            }

                            String ngram = word.substring(0, k);
                            if(!lex_map.containsKey(ngram)) {
                                lex_map.put(ngram, Integer.valueOf(lex_map.size()));
                            }

                            this.lex2tid.put(Integer.valueOf(k), lex_map);
                        }

                        String last_ch = word.substring(word.length() - 1, word.length());
                        lex_map = (HashMap)this.lex2tid.get(new Integer(4));
                        if(lex_map == null) {
                            lex_map = new HashMap();
                        }

                        if(!lex_map.containsKey(last_ch)) {
                            lex_map.put(last_ch, Integer.valueOf(lex_map.size()));
                        }

                        this.lex2tid.put(Integer.valueOf(4), lex_map);
                    }
                } else {
                    for(i = 1; i < items.length; ++i) {
                        wpos = items[i].trim().split("\\|");
                        word = wpos[0].trim();
                        pos = wpos[1].trim();
                        n2 = (Integer)this.mention_freq_test.get(word);
                        if(n2 == null) {
                            n2 = Integer.valueOf(0);
                        }

                        this.mention_freq_test.put(word, Integer.valueOf(n2.intValue() + 1));
                        if(this.mset_test.isExists(word)) {
                            men = this.mset_test.getMention(word);
                            men.addTid(tid);
                        } else {
                            men = new Mention(word);
                            men.addTid(tid);
                            this.mset_test.addMention(word, men);
                        }

                        hmap = (HashMap)this.mention_test.get(word);
                        if(hmap == null) {
                            hmap = new HashMap();
                        }

                        n = (Integer)hmap.get(pos);
                        if(n == null) {
                            n = Integer.valueOf(0);
                        }

                        hmap.put(pos, Integer.valueOf(n.intValue() + 1));
                        this.mention_test.put(word, hmap);
                    }
                }
            }

            br.close();
            return;
        }
    }

    private void init_dict() throws Exception {
        String first_name_path = "./data/CHdict/FirstName.txt";
        this.first_name = new Dictionary(first_name_path);
        System.out.println("the size of first name list: " + this.first_name.size());
        String last_name_path = "./data/CHdict/LastName.txt";
        this.last_name = new Dictionary(last_name_path);
        System.out.println("the size of last name list: " + this.last_name.size());
        String per_name_path1 = "./data/CHdict/PERNAME.txt";
        String per_name_path2 = "./data/CHdict/TDTPER.txt";
        String per_name_path3 = "./data/CHdict/PERList.txt";
        this.per_name = new Dictionary(per_name_path1);
        this.per_name.add(per_name_path2);
        this.per_name.add(per_name_path3);
        System.out.println("the size of person name list: " + this.per_name.size());
        String per_suffix_path = "./data/CHdict/PERSuffix.txt";
        this.per_suffix = new Dictionary(per_suffix_path);
        String gpe_name_path1 = "./data/CHdict/GPENAME.txt";
        String gpe_name_path2 = "./data/CHdict/Location.txt";
        String gpe_name_path3 = "./data/CHdict/TDT4LOC.txt";
        String gpe_name_path4 = "./data/CHdict/Gpeprop.txt";
        String gpe_name_path5 = "./data/CHdict/LOCNAME.txt";
        String gpe_name_path6 = "./data/CHdict/TDT4GPE.txt";
        String gpe_name_path7 = "./data/CHdict/GPEList.txt";
        this.gpe_name = new Dictionary(gpe_name_path1);
        this.gpe_name.add(gpe_name_path2);
        this.gpe_name.add(gpe_name_path3);
        this.gpe_name.add(gpe_name_path4);
        this.gpe_name.add(gpe_name_path5);
        this.gpe_name.add(gpe_name_path6);
        this.gpe_name.add(gpe_name_path7);
        System.out.println("the size of GPE name list: " + this.gpe_name.size());
        String gpe_suffix_path1 = "./data/CHdict/GPESuffix.txt";
        String gpe_suffix_path2 = "./data/CHdict/GpeRef.txt";
        String gpe_suffix_path3 = "./data/CHdict/LocRef.txt";
        this.gpe_suffix = new Dictionary(gpe_suffix_path1);
        this.gpe_suffix.add(gpe_suffix_path2);
        this.gpe_suffix.add(gpe_suffix_path3);
        System.out.println("the size of gpe suffix list: " + this.gpe_suffix.size());
        String fac_name_path = "./data/CHdict/FACNAME.txt";
        this.fac_name = new Dictionary(fac_name_path);
        System.out.println("the size of facility: " + this.fac_name.size());
        String nationality_path = "./data/CHdict/Nationality.txt";
        this.nationality = new Dictionary(nationality_path);
        System.out.println("the size of nationality list: " + this.nationality.size());
        String norm_list_path1 = "./data/CHdict/NOMList.txt";
        String norm_list_path2 = "./data/CHdict/NormalWord.txt";
        String norm_list_path3 = "./data/CHdict/NormalWord_add.txt";
        String norm_list_path4 = "./data/CHdict/NormalWordwith1.txt";
        this.nom_list = new Dictionary(norm_list_path1);
        this.nom_list.add(norm_list_path2);
        this.nom_list.add(norm_list_path3);
        this.nom_list.add(norm_list_path4);
        System.out.println("the size of norminal name list: " + this.nom_list.size());
        String org_name_path1 = "./data/CHdict/ORGNAME.txt";
        String org_name_path2 = "./data/CHdict/TDT4ORG.txt";
        String org_name_path3 = "./data/CHdict/ORGList.txt";
        this.org_name = new Dictionary(org_name_path1);
        this.org_name.add(org_name_path3);
        this.org_name.add(org_name_path2);
        System.out.println("the size of org name list: " + this.org_name.size());
        String org_suffix_path1 = "./data/CHdict/OrgRef.txt";
        String org_suffix_path2 = "./data/CHdict/ORGSuffix.txt";
        this.org_suffix = new Dictionary(org_suffix_path1);
        this.org_suffix.add(org_suffix_path2);
        System.out.println("the size of org suffix list: " + this.org_suffix.size());
        String stop_name_path = "./data/CHdict/StopName.txt";
        this.stop_name = new Dictionary(stop_name_path);
        System.out.println("the size of stop name: " + this.stop_name.size());
        String dict_path = "./data/CHdict/dict.txt";
        String wikititle_path = "./data/wikititles.txt";
        this.dict = new Dictionary(dict_path);
        this.dict.add(wikititle_path);
    }

    public void read_ners(String inputpath) throws Exception {
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;

        while((str = br.readLine()) != null) {
            String[] items = str.split("\t");
            String name = items[0].trim();
            String tag = items[1].trim();
            this.ners.put(name, tag);
        }

        br.close();
    }

    public void construct_tweet_ner_set(String inputpath) throws Exception {
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;

        while((str = br.readLine()) != null) {
            String[] items = str.trim().split("\t");
            String tid = items[0].trim();
            TweetNer tner = new TweetNer(tid);

            for(int i = 1; i < items.length; ++i) {
                String item = items[i].trim();
                String[] items2 = item.split("\\|");
                String word = items2[0].trim();
                String tag = (String)this.ners.get(word);
                if(tag != null) {
                    tner.addNer(word);
                    if(tag.equals("nr")) {
                        tner.addPer(word);
                    } else if(tag.equals("ns")) {
                        tner.addGPE(word);
                    } else {
                        tner.addOrg(word);
                    }
                }
            }

            this.ner_set.addTweetNer(tid, tner);
        }

        br.close();
    }

    public void read_tids(String inputpath, HashSet<String> set) throws Exception {
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;

        while((str = br.readLine()) != null) {
            str = str.trim();
            set.add(str);
        }

        br.close();
    }

    public void read_annotation(String inputpath) throws Exception {
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;

        while((str = br.readLine()) != null) {
            str = str.trim();
            String[] items = str.split("\t");
            String mention = items[0].trim();
            HashSet<String> set = new HashSet();

            for(int i = 1; i < items.length; ++i) {
                String tid = items[i].trim();
                set.add(tid);
            }

            this.anns.put(mention, set);
        }

        br.close();
    }

    public void read_pinyin_dict(String inputpath) throws Exception {
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;

        while((str = br.readLine()) != null) {
            str = str.trim();
            String[] items = str.split("\t");
            String w_pinyin = items[0].trim();
            HashSet<String> set = (HashSet)this.pinyin_dict.get(w_pinyin);
            if(set == null) {
                set = new HashSet<>();
            }

            for(int i = 1; i < items.length; ++i) {
                String word = items[i].trim();
                set.add(word);
            }

            this.pinyin_dict.put(w_pinyin, set);
        }

        br.close();
    }
}
