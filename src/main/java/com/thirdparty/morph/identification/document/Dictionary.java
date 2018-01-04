package com.thirdparty.morph.identification.document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

/**
 * Created by helencoder on 2018/1/4.
 */
public class Dictionary {
    private HashSet<String> hset;


    public Dictionary(String inputpath)throws Exception{
        File file=new File(inputpath);
        BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
        String str=null;
        hset=new HashSet<String>();
        while((str=br.readLine())!=null){
            String []items=str.trim().split("\t");
            this.hset.add(items[0].trim());
        }
        br.close();
    }

    public void add(String inputpath)throws Exception{
        File file=new File(inputpath);
        BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
        String str=null;
        if(hset==null) hset=new HashSet<String>();
        while((str=br.readLine())!=null){
            String []items=str.trim().split("\t");
            this.hset.add(items[0].trim());
        }
        br.close();
    }

    public boolean contains(String item){
        return this.hset.contains(item);
    }

    public int size(){
        return this.hset.size();
    }

    public HashSet<String> getDict(){
        return this.hset;
    }
}
