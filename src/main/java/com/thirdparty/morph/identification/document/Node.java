package com.thirdparty.morph.identification.document;

/**
 * Created by helencoder on 2018/1/4.
 */
public class Node {
    private String mention;
    private String id;
    private int pos;

    public Node(String mention, String id, int pos) {
        this.mention = mention;
        this.id = id;
        this.pos = pos;
    }

    public String getMention() {
        return this.mention;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPos() {
        return this.pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
