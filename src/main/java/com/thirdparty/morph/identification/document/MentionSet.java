package com.thirdparty.morph.identification.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by helencoder on 2018/1/4.
 */
public class MentionSet {
    private HashMap<String, Integer> name2index;
    private ArrayList<Mention> mentions;
    private HashSet<String> mention_set;

    public MentionSet(){
        this.name2index=new HashMap<String, Integer>();
        this.mentions=new ArrayList<Mention>();
        this.mention_set=new HashSet<String>();
    }

    public void addMention(String name, Mention m){
        int index=this.mentions.size();
        this.name2index.put(name, index);
        this.mentions.add(m);
        this.mention_set.add(name);
    }

    public Mention getMention(String name){
        Integer index=this.name2index.get(name);
        if(index!=null)return this.mentions.get(index);
        else return null;
    }

    public Mention getDocument(int index){
        if(index>=this.mentions.size() || index<0)return null;
        else return this.mentions.get(index);
    }
    public int size(){
        return this.mentions.size();
    }

    public ArrayList<Mention> getAllMentions(){
        return this.mentions;
    }

    public boolean isExists(String name){
        if(this.mention_set.contains(name))return true;
        else return false;
    }
}
