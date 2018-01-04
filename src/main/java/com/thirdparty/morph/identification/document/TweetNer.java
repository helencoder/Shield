package com.thirdparty.morph.identification.document;

import java.util.HashSet;

/**
 * Created by helencoder on 2018/1/4.
 */
public class TweetNer {
    private String tid;
    private HashSet<String> ners;
    private HashSet<String> pers;
    private HashSet<String> orgs;
    private HashSet<String> gpes;

    public TweetNer(String tid){
        this.tid=tid;
        this.ners=new HashSet<String>();
        this.pers=new HashSet<String>();
        this.orgs=new HashSet<String>();
        this.gpes=new HashSet<String>();
    }

    public void addNer(String name){
        this.ners.add(name);
    }

    public void addPer(String name){
        this.ners.add(name);
    }
    public void addOrg(String name){
        this.orgs.add(name);
    }

    public void addGPE(String name){
        this.gpes.add(name);
    }

    public HashSet<String> getNers(){
        return this.ners;
    }

    public HashSet<String> getPers(){
        return this.pers;
    }

    public HashSet<String> getOrgs(){
        return this.orgs;
    }

    public HashSet<String> getGPEs(){
        return this.gpes;
    }
}
