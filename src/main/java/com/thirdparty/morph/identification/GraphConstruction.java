package com.thirdparty.morph.identification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.thirdparty.morph.identification.document.Node;
import com.thirdparty.morph.identification.document.Tweet;
import com.thirdparty.morph.identification.document.TweetProcessing;
import com.thirdparty.morph.identification.document.TweetSet;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;


/**
 * Created by helencoder on 2018/1/4.
 */
public class GraphConstruction {
    private ArrayList<Node> nodes = new ArrayList();
    private ArrayList<Integer> labels = new ArrayList();
    private ArrayList<Double> init_rank = new ArrayList();
    private TweetSet tweetset;
    private HashSet<String> lablled_tweets;
    private HashSet<String> morphs = new HashSet();
    private int l = 0;

    public static void main(String[] args) throws Exception {
        String labelled_path = "./data/exp/tweets/train";
        String tweet_path = "./data/exp/tweets/preprocess.txt";
        String user_path = "./data/exp/tweets/t2u_exp";
        String train_mention_path = "./data/exp/discovery/train.mention";
        String test_rank_path = "./data/exp/discovery/test/morphs.output";
        double threshold = (new Double(0.1)).doubleValue();
        GraphConstruction gc = new GraphConstruction(labelled_path, tweet_path, user_path, train_mention_path, test_rank_path, threshold);
        gc.read_nodes(tweet_path);
        String node_path = "./data/exp/verification/network/node.graph";
        String init_rank_path = "./data/exp/verification/network/init_rank";
        gc.print_init_ranking(init_rank_path, node_path);
        String W0_path = "./data/exp/verification/network/w0";
        gc.contruct_conreference_context_graph(W0_path);
        String W1_path = "./data/exp/verification/network/w1";
        gc.construct_conreference_social_graph(W1_path);
        String W2_path = "./data/exp/verification/network/w2";
        gc.construct_corelation_graph(W2_path);
    }

    public GraphConstruction(String labelled_path, String tweet_path, String user_path, String train_mention_path, String rank_path, double threshold) throws Exception {
        TweetProcessing tp = new TweetProcessing();
        this.tweetset = tp.getTweetSet(tweet_path, user_path);
        this.lablled_tweets = new HashSet();
        this.read_lablled_tweets(labelled_path);
        this.read_train_morphs(train_mention_path);
        this.read_test_morphs(rank_path, threshold);
    }

    public void construct_corelation_graph(String outputpath) throws Exception {
        int len = this.nodes.size();
        RealMatrix W = new Array2DRowRealMatrix(len, len);

        for(int i = 0; i < this.nodes.size(); ++i) {
            Node node1 = (Node)this.nodes.get(i);
            String tid1 = node1.getId();
            Tweet t1 = this.tweetset.getDocument(tid1);
            HashSet<String> users1 = t1.getUsers();
            String mention1 = node1.getMention();

            for(int j = i + 1; j < this.nodes.size(); ++j) {
                Node node2 = (Node)this.nodes.get(j);
                String tid2 = node2.getId();
                Tweet t2 = this.tweetset.getDocument(tid2);
                HashSet<String> users2 = t2.getUsers();
                String mention2 = node2.getMention();
                if((tid1.equals(tid2) || this.intersection(users1, users2) > 0) && !mention1.equals(mention2)) {
                    W.setEntry(i, j, 1.0D);
                    W.setEntry(j, i, 1.0D);
                }
            }
        }

        this.print_matrix(outputpath, W);
        System.out.println("finished contructing correlation graph!");
    }

    public void construct_conreference_social_graph(String outputpath) throws Exception {
        int len = this.nodes.size();
        RealMatrix W = new Array2DRowRealMatrix(len, len);

        for(int i = 0; i < this.nodes.size(); ++i) {
            Node node1 = (Node)this.nodes.get(i);
            String tid1 = node1.getId();
            Tweet t1 = this.tweetset.getDocument(tid1);
            HashSet<String> users1 = t1.getUsers();
            String mention1 = node1.getMention();

            for(int j = i + 1; j < this.nodes.size(); ++j) {
                Node node2 = (Node)this.nodes.get(j);
                String tid2 = node2.getId();
                Tweet t2 = this.tweetset.getDocument(tid2);
                HashSet<String> users2 = t2.getUsers();
                String mention2 = node2.getMention();
                if(mention1.equals(mention2) && this.intersection(users1, users2) > 0) {
                    W.setEntry(i, j, 1.0D);
                    W.setEntry(j, i, 1.0D);
                }
            }
        }

        this.print_matrix(outputpath, W);
        System.out.println("finished contructing social coreference graph!");
    }

    private int intersection(HashSet<String> set1, HashSet<String> set2) {
        if(set1 != null && set2 != null) {
            int num = 0;
            Iterator it = set1.iterator();

            while(it.hasNext()) {
                String tag1 = (String)it.next();
                if(set2.contains(tag1)) {
                    ++num;
                }
            }

            return num;
        } else {
            return 0;
        }
    }

    public void contruct_conreference_context_graph(String outputpath) throws Exception {
        int len = this.nodes.size();
        RealMatrix W = new Array2DRowRealMatrix(len, len);

        for(int i = 0; i < this.nodes.size(); ++i) {
            Node node1 = (Node)this.nodes.get(i);
            String mention1 = node1.getMention();
            HashMap<String, Double> fmap1 = this.compute_local_features(node1);

            for(int j = i + 1; j < this.nodes.size(); ++j) {
                Node node2 = (Node)this.nodes.get(j);
                String mention2 = node2.getMention();
                if(mention1.equals(mention2)) {
                    HashMap<String, Double> fmap2 = this.compute_local_features(node2);
                    double sim = this.consine_sim(fmap1, fmap2);
                    W.setEntry(i, j, sim);
                    W.setEntry(j, i, sim);
                }
            }
        }

        int k = 10;
        RealMatrix W_knn = this.KNN_graph(W, k);
        this.print_matrix(outputpath, W_knn);
        System.out.println("finished contructing contextual coreference graph!");
    }

    private HashMap<String, Double> compute_local_features(Node node) {
        HashMap<String, Double> fmap = new HashMap();
        String mention = node.getMention();
        String tid = node.getId();
        Tweet t = this.tweetset.getDocument(tid);
        HashSet<String> tokens = t.gotTokensSet();
        Iterator it = tokens.iterator();

        while(it.hasNext()) {
            String token = (String)it.next();
            fmap.put(token, Double.valueOf(1.0D));
        }

        fmap.remove(mention);
        return fmap;
    }

    public RealMatrix KNN_graph(RealMatrix m, int k) throws Exception {
        int row = m.getRowDimension();
        int col = m.getColumnDimension();
        RealMatrix m2 = new Array2DRowRealMatrix(row, col);

        for(int i = 0; i < row; ++i) {
            HashSet<Integer> set = new HashSet();

            for(int n = 0; n < k; ++n) {
                double max = -1.0D;
                int index = -1;

                for(int j = 0; j < col; ++j) {
                    if(!set.contains(new Integer(j)) && m.getEntry(i, j) > max) {
                        max = m.getEntry(i, j);
                        index = j;
                    }
                }

                set.add(new Integer(index));
            }

            Iterator it = set.iterator();

            while(it.hasNext()) {
                Integer j = (Integer)it.next();
                m2.setEntry(i, j.intValue(), m.getEntry(i, j.intValue()));
            }
        }

        return m2;
    }

    public void print_graph(RealMatrix W, String outputpath) throws Exception {
        File output_file = new File(outputpath);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_file), "UTF-8"));
        int correct = 0;
        int total = 0;

        for(int i = this.l; i < this.nodes.size(); ++i) {
            Node node1 = (Node)this.nodes.get(i);
            String tid1 = node1.getId();
            String mention1 = node1.getMention();
            Integer label1 = (Integer)this.labels.get(i);
            boolean check = false;

            for(int j = 0; j < this.nodes.size(); ++j) {
                Node node2 = (Node)this.nodes.get(j);
                String tid2 = node2.getId();
                String mention2 = node2.getMention();
                Integer label2 = (Integer)this.labels.get(j);
                double d = W.getEntry(i, j);
                if(d > 0.0D) {
                    ++total;
                }

                if(d > 0.0D && !label1.equals(label2)) {
                    bw.write("(" + mention2 + "|" + tid2 + "|" + d + ")\t");
                    ++correct;
                    check = true;
                }
            }

            if(check) {
                bw.write("$$(" + mention1 + "|" + tid1 + ")\n");
            }
        }

        bw.close();
        double p = ((double)correct + 0.0D) / (double)total;
        System.out.println("total: " + total + " wrong: " + correct + " " + p);
    }

    public void print_matrix(String outputpath, RealMatrix m) throws Exception {
        File output_file2 = new File(outputpath);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_file2), "UTF-8"));
        int rd = m.getRowDimension();
        int cd = m.getColumnDimension();

        for(int i = 0; i < rd; ++i) {
            for(int j = 0; j < cd; ++j) {
                bw.write(m.getEntry(i, j) + "\t");
            }

            bw.write("\n");
        }

        bw.close();
    }

    public void print_init_ranking(String init_rank_path, String node_path) throws Exception {
        this.print_arr(init_rank_path, this.init_rank);
        this.print_nodes(node_path, this.nodes);
    }

    public void print_arr(String outputpath, ArrayList<Double> arr) throws Exception {
        File output_file2 = new File(outputpath);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_file2), "UTF-8"));

        for(int i = 0; i < arr.size(); ++i) {
            bw.write(arr.get(i) + "\n");
            if(i + 1 == this.l) {
                bw.write("\n");
            }
        }

        bw.close();
    }

    public void print_nodes(String outputpath, ArrayList<Node> arr) throws Exception {
        File output_file2 = new File(outputpath);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_file2), "UTF-8"));

        for(int i = 0; i < arr.size(); ++i) {
            Node node = (Node)arr.get(i);
            bw.write(node.getId() + "\t" + node.getMention() + "\t" + "\n");
        }

        bw.close();
    }

    public void read_nodes(String inputpath) throws Exception {
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;
        ArrayList<Node> unlabelled_nodes = new ArrayList();
        ArrayList<Double> unlabelled_scores = new ArrayList();
        ArrayList unlabelled_labels = new ArrayList();

        while(true) {
            while((str = br.readLine()) != null) {
                str = str.trim();
                String[] items = str.split("\t");
                String tid = items[0].trim();
                int i;
                String item;
                String[] anns;
                String token;
                String pos;
                Node node;
                if(this.lablled_tweets.contains(tid)) {
                    for(i = 1; i < items.length; ++i) {
                        item = items[i].trim();
                        anns = item.split("\\|");
                        token = anns[0].trim();
                        pos = anns[1].trim();
                        if(this.morphs.contains(token)) {
                            node = new Node(token, tid, i - 1);
                            this.nodes.add(node);
                            ++this.l;
                            if(anns.length == 3) {
                                Integer label = new Integer(anns[2].trim());
                                this.init_rank.add(Double.valueOf((double)label.intValue() + 0.0D));
                                this.labels.add(label);
                            } else {
                                this.init_rank.add(Double.valueOf(0.0D));
                                this.labels.add(Integer.valueOf(0));
                            }
                        }
                    }
                } else {
                    for(i = 1; i < items.length; ++i) {
                        item = items[i].trim();
                        anns = item.split("\\|");
                        token = anns[0].trim();
                        if(this.morphs.contains(token)) {
                            pos = anns[1].trim();
                            if(anns.length == 3) {
                                Integer label2 = new Integer(anns[2].trim());
                                unlabelled_labels.add(label2);
                            } else {
                                unlabelled_labels.add(Integer.valueOf(0));
                            }

                            node = new Node(token, tid, i - 1);
                            unlabelled_nodes.add(node);
                            unlabelled_scores.add(Double.valueOf(0.5D));
                        }
                    }
                }
            }

            for(int i = 0; i < unlabelled_nodes.size(); ++i) {
                this.nodes.add((Node)unlabelled_nodes.get(i));
                this.init_rank.add((Double)unlabelled_scores.get(i));
                this.labels.add((Integer)unlabelled_labels.get(i));
            }

            br.close();
            System.out.println("the total number of nodes is: " + this.nodes.size() + "\t" + this.init_rank.size() + "\t" + " lablled nodes: " + this.l);
            return;
        }
    }

    public void read_lablled_tweets(String inputpath) throws Exception {
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;

        while((str = br.readLine()) != null) {
            str = str.trim();
            this.lablled_tweets.add(str);
        }

        br.close();
    }

    private double consine_sim(HashMap<String, Double> fmap1, HashMap<String, Double> fmap2) {
        if(fmap1 != null && fmap2 != null) {
            HashSet<String> map = new HashSet();
            Iterator it = fmap1.keySet().iterator();

            while(it.hasNext()) {
                String word = (String)it.next();
                map.add(word);
            }

            Iterator it2 = fmap2.keySet().iterator();

            while(it2.hasNext()) {
                String word = (String)it2.next();
                map.add(word);
            }

            ArrayList<String> feature = new ArrayList();
            Iterator it3 = map.iterator();

            while(it3.hasNext()) {
                String word = (String)it3.next();
                feature.add(word);
            }

            ArrayList<Double> feature1 = new ArrayList();
            ArrayList<Double> feature2 = new ArrayList();

            for(int i = 0; i < feature.size(); ++i) {
                String word = (String)feature.get(i);
                Double d1 = (Double)fmap1.get(word);
                if(d1 == null) {
                    feature1.add(Double.valueOf(0.0D));
                } else {
                    feature1.add(d1);
                }

                Double d2 = (Double)fmap2.get(word);
                if(d2 == null) {
                    feature2.add(Double.valueOf(0.0D));
                } else {
                    feature2.add(d2);
                }
            }

            return this.consine(feature1, feature2);
        } else {
            return 0.0D;
        }
    }

    private double consine(ArrayList<Double> feature1, ArrayList<Double> feature2) {
        if(feature1.size() != 0 && feature2.size() != 0) {
            double sum = 0.0D;
            double sum1 = 0.0D;
            double sum2 = 0.0D;

            for(int i = 0; i < feature1.size(); ++i) {
                Double d1 = (Double)feature1.get(i);
                Double d2 = (Double)feature2.get(i);
                sum += d1.doubleValue() * d2.doubleValue();
                sum1 += d1.doubleValue() * d1.doubleValue();
                sum2 += d2.doubleValue() * d2.doubleValue();
            }

            if(sum1 != 0.0D && sum2 != 0.0D) {
                double sim = sum / (Math.sqrt(sum1) * Math.sqrt(sum2));
                return sim;
            } else {
                return 0.0D;
            }
        } else {
            return 0.0D;
        }
    }

    public void row_normalize_matrix(RealMatrix m) {
        int rd = m.getRowDimension();
        int cd = m.getColumnDimension();

        for(int i = 0; i < rd; ++i) {
            double sum = 0.0D;

            int j;
            for(j = 0; j < cd; ++j) {
                sum += m.getEntry(i, j);
            }

            if(sum > 0.0D) {
                for(j = 0; j < cd; ++j) {
                    double v = m.getEntry(i, j) / sum;
                    m.setEntry(i, j, v);
                }
            }
        }

    }

    public void read_train_morphs(String mention_path) throws Exception {
        File file = new File(mention_path);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;

        while((str = br.readLine()) != null) {
            String[] items = str.trim().split("\t");
            String mention = items[0].trim();
            Integer label = new Integer(items[1].trim());
            if(label.intValue() == 1) {
                this.morphs.add(mention);
            }
        }

        br.close();
    }

    public void read_test_morphs(String rank_path, double threshold) throws Exception {
        File file = new File(rank_path);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;

        while((str = br.readLine()) != null) {
            String[] items = str.trim().split("\t");
            String mention = items[0].trim();
            Double p = new Double(items[2].trim());
            if(p.doubleValue() >= threshold) {
                this.morphs.add(mention);
            }
        }

        br.close();
    }
}
