package com.thirdparty.morph.identification.document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
/**
 * Created by helencoder on 2018/1/4.
 */
public class TweetProcessing {
    private HashMap<String, HashSet<String>> t2u = new HashMap();
    private TweetSet tweetset = new TweetSet();

    public TweetProcessing() throws Exception {
    }

    public TweetSet getTweetSet(String tweet_path, String user_path) throws Exception {
        this.read_users(user_path);
        File file = new File(tweet_path);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;

        while((str = br.readLine()) != null) {
            str = str.trim();
            String[] items = str.split("\t");
            String tid = items[0].trim();
            HashSet<String> users = (HashSet)this.t2u.get(tid);
            Tweet t = this.getTweet(items);
            t.setUsers(users);
            this.tweetset.addDocument(tid, t);
        }

        br.close();
        System.out.println("finished processing tweets");
        return this.tweetset;
    }

    private Tweet getTweet(String[] items) {
        String tid = items[0].trim();
        ArrayList<String> tokens = new ArrayList();
        ArrayList<String> postags = new ArrayList();
        HashSet<String> tokens_set = new HashSet();

        for(int i = 1; i < items.length; ++i) {
            String item = items[i].trim();
            String[] strs = item.split("\\|");
            String word = strs[0].trim();
            String pos = strs[1].trim();
            tokens.add(word);
            postags.add(pos);
            tokens_set.add(word);
        }

        Tweet t = new Tweet(tid, tokens, postags, tokens_set);
        return t;
    }

    public void read_users(String inputpath) throws Exception {
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;

        while((str = br.readLine()) != null) {
            str = str.trim();
            String[] items = str.split("\t");
            String tid = items[0].trim();
            HashSet<String> set = new HashSet();

            for(int i = 1; i < items.length; ++i) {
                set.add(items[i].trim());
            }

            this.t2u.put(tid, set);
        }

        br.close();
    }
}
