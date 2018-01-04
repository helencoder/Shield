package com.thirdparty.morph.identification.libsvm;

import java.io.*;

/**
 * Created by helencoder on 2018/1/4.
 */
public class OutputGeneration {
    public static void main(String []args)throws Exception{
        String rank_path = args[0];
        String mention_path = args[1];
        String outputpath = args[2];

        File file = new File(rank_path);
        BufferedReader br=new BufferedReader(new InputStreamReader(
                new FileInputStream(file),"UTF-8"));
        File file2 = new File(mention_path);
        BufferedReader br2=new BufferedReader(new InputStreamReader(
                new FileInputStream(file2),"UTF-8"));
        File output_file = new File(outputpath);
        BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(output_file), "UTF-8"));

        String str=br.readLine();
        String str2=null;
        while((str=br.readLine())!=null){
            str2=br2.readLine();
            str=str.trim();
            String []items=str.split(" ");
            String []items2=str2.split("\t");
            Double plabel=new Double(items[0].trim());
            Double p=new Double(items[2].trim());
            String word=items2[0].trim();
            bw.write(word+"\t"+plabel+"\t"+p+"\n");

        }
        br.close();
        br2.close();
        bw.close();
    }
}
