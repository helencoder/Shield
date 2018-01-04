package com.thirdparty.morph.identification.document;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * Created by helencoder on 2018/1/4.
 */
public class TweetSet {
    private HashMap<String, Integer> id2index = new HashMap();
    private ArrayList<Tweet> tweets = new ArrayList();

    public TweetSet() {
    }

    public void addDocument(String tid, Tweet t) {
        int index = this.tweets.size();
        this.id2index.put(tid, Integer.valueOf(index));
        this.tweets.add(t);
    }

    public Tweet getDocument(String tid) {
        Integer index = (Integer)this.id2index.get(tid);
        return index != null?(Tweet)this.tweets.get(index.intValue()):null;
    }

    public Tweet getDocument(int index) {
        return index < this.tweets.size() && index >= 0?(Tweet)this.tweets.get(index):null;
    }

    public int size() {
        return this.tweets.size();
    }

    public ArrayList<Tweet> getAllTweets() {
        return this.tweets;
    }
}
