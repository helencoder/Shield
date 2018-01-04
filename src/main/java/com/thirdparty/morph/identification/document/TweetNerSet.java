package com.thirdparty.morph.identification.document;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by helencoder on 2018/1/4.
 */
public class TweetNerSet {
    private HashMap<String, Integer> id2index;
    private ArrayList<TweetNer> tweetners;

    public TweetNerSet(){
        this.id2index=new HashMap<String, Integer>();
        this.tweetners=new ArrayList<TweetNer>();
    }

    public void addTweetNer(String tid, TweetNer tn){
        int index=this.tweetners.size();
        this.id2index.put(tid, index);
        this.tweetners.add(tn);
    }

    public TweetNer getTweetNer(String tid){
        Integer index=this.id2index.get(tid);
        if(index!=null)return this.tweetners.get(index);
        else return null;
    }

    public TweetNer getTweetNer(int index){
        if(index>=this.tweetners.size() || index<0)return null;
        else return this.tweetners.get(index);
    }
    public int size(){
        return this.tweetners.size();
    }

    public ArrayList<TweetNer> getAllTweetNers(){
        return this.tweetners;
    }
}
