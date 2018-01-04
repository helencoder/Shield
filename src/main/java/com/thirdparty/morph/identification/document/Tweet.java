package com.thirdparty.morph.identification.document;

import java.util.ArrayList;
import java.util.HashSet;
/**
 * Created by helencoder on 2018/1/4.
 */
public class Tweet {
    private String id;
    private ArrayList<String> tokens;
    private HashSet<String> set_tokens;
    private ArrayList<String> postags;
    private HashSet<String> users;

    public Tweet(String id, ArrayList<String> tokens, ArrayList<String> postags, HashSet<String> set_tokens) {
        this.id = id;
        this.tokens = tokens;
        this.postags = postags;
        this.set_tokens = set_tokens;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getTokens() {
        return this.tokens;
    }

    public void setTokens(ArrayList<String> tokens) {
        this.tokens = tokens;
    }

    public ArrayList<String> getPostags() {
        return this.postags;
    }

    public void setPostags(ArrayList<String> postags) {
        this.postags = postags;
    }

    public void setUsers(HashSet<String> users) {
        this.users = users;
    }

    public HashSet<String> getUsers() {
        return this.users;
    }

    public HashSet<String> gotTokensSet() {
        return this.set_tokens;
    }

    public void setTokensSet(HashSet<String> set_tokens) {
        this.set_tokens = set_tokens;
    }
}
