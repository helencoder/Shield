package com.thirdparty.morph.identification.document;

import java.util.HashSet;

/**
 * Created by helencoder on 2018/1/4.
 */
public class Mention {
    private String mention;
    private HashSet<String> tids;

    public Mention(String mention){
        this.mention=mention;
        this.tids=new HashSet<String>();
    }
    public String getMention(){
        return this.mention;
    }

    public void addTid(String tid){
        this.tids.add(tid);
    }

    public HashSet<String> getTids(){
        return this.tids;
    }
}
