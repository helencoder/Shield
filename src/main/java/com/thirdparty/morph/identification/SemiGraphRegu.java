package com.thirdparty.morph.identification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
/**
 * Created by helencoder on 2018/1/4.
 */
public class SemiGraphRegu {
    private int l;
    private int[] lablled;
    private int[] unlabelled;
    private ArrayList<String> nodes;

    public SemiGraphRegu() {
    }

    public static void main(String[] args) throws Exception {
        int N = 50;
        SemiGraphRegu gr = new SemiGraphRegu();
        String outputpath = "./data/exp/verification/verification.output2";
        gr.graph_regu_iter(outputpath);
    }

    public void graph_regu_iter(String outputpath) throws Exception {
        String node_path = "./data/exp/verification/network/node.graph";
        String initial_path = "./data/exp/verification/network/init_rank";
        String w0_path = "./data/exp/verification/network/w0";
        String w1_path = "./data/exp/verification/network/w1";
        String w2_path = "./data/exp/verification/network/w2";
        this.read_nodes(node_path);
        RealMatrix R0 = this.read_vector(initial_path);
        RealMatrix W0 = this.read_weighted_matrix(w0_path);
        RealMatrix W1 = this.read_weighted_matrix(w1_path);
        RealMatrix W2 = this.read_weighted_matrix(w2_path);
        this.row_normalize_matrix(W0);
        this.row_normalize_matrix(W1);
        this.row_normalize_matrix(W2);
        RealMatrix W_coref = W0.add(W1);
        double mu = 0.1D;
        double alpha = 0.8D;
        double beta = 1.0D - alpha;
        int len = W0.getRowDimension();
        this.init_label(len);
        int[] col = new int[]{0};
        RealMatrix W = W_coref.scalarMultiply(alpha).add(W2.scalarMultiply(beta));
        this.normalize_matrix(W);
        RealMatrix D = this.diagonal_matrix(W);
        RealMatrix I = this.identity_matrix(len);
        RealMatrix D_uu = D.getSubMatrix(this.unlabelled, this.unlabelled);
        RealMatrix I_uu = I.getSubMatrix(this.unlabelled, this.unlabelled);
        System.out.println("finish computing diag matrix!");
        RealMatrix m_uu = D_uu.add(I_uu.scalarMultiply(mu));
        RealMatrix W_uu = W.getSubMatrix(this.unlabelled, this.unlabelled);
        RealMatrix W_ul = W.getSubMatrix(this.unlabelled, this.lablled);
        RealMatrix Ru0 = R0.getSubMatrix(this.unlabelled, col);
        RealMatrix Rl = R0.getSubMatrix(this.lablled, col);
        RealMatrix m4 = W_ul.multiply(Rl);
        RealMatrix m5 = Ru0.scalarMultiply(mu);
        RealMatrix m_inv = this.inverse_diag(m_uu);
        System.out.println("finish computing inverse matrix");
        double minRes = 1.0E-5D;
        double res = 1.0D;
        int maxStep = 100;
        int iter = 0;
        RealMatrix Ru = Ru0.copy();

        for(RealMatrix Ru_prev = Ru.copy(); iter < maxStep && res > minRes; Ru_prev = Ru) {
            ++iter;
            RealMatrix m6 = W_uu.multiply(Ru_prev).add(m4).add(m5);
            Ru = m_inv.multiply(m6);
            res = this.residual(Ru, Ru_prev);
        }

        File output_file2 = new File(outputpath);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_file2), "UTF-8"));
        this.print(Rl, 0, bw);
        this.print(Ru, this.l, bw);
        bw.close();
        System.out.println("finished mention verification.");
    }

    private void init_label(int len) {
        this.lablled = new int[this.l];
        this.unlabelled = new int[len - this.l];

        int i;
        for(i = 0; i < this.l; this.lablled[i] = i++) {
            ;
        }

        for(i = 0; i < len - this.l; ++i) {
            this.unlabelled[i] = this.l + i;
        }

    }

    private RealMatrix inverse_diag(RealMatrix m) {
        int len = m.getRowDimension();
        RealMatrix m_inv = new Array2DRowRealMatrix(len, len);
        double sum = 0.0D;

        for(int i = 0; i < len; ++i) {
            m_inv.setEntry(i, i, 1.0D / m.getEntry(i, i));
        }

        return m_inv;
    }

    private double residual(RealMatrix m1, RealMatrix m2) {
        int row = m1.getRowDimension();
        double sum = 0.0D;

        for(int i = 0; i < row; ++i) {
            sum += Math.abs(m1.getEntry(i, 0) - m2.getEntry(i, 0));
        }

        return sum;
    }

    public void print(RealMatrix R, int start, BufferedWriter bw) throws Exception {
        DecimalFormat df = new DecimalFormat("#.00");
        int len = R.getRowDimension();

        for(int i = 0; i < len; ++i) {
            bw.write((String)this.nodes.get(start + i) + "\t" + df.format(R.getEntry(i, 0)) + "\n");
        }

    }

    public void read_nodes(String inputpath) throws Exception {
        this.nodes = new ArrayList();
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;

        while((str = br.readLine()) != null) {
            str = str.trim();
            this.nodes.add(str);
        }

        br.close();
        System.out.println("node size: " + this.nodes.size());
    }

    public RealMatrix read_weighted_matrix(String inputpath) throws Exception {
        int len = this.length(inputpath);
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = null;
        RealMatrix m = new Array2DRowRealMatrix(len, len);
        int i = 0;

        int num;
        for(num = 0; (str = br.readLine()) != null; ++i) {
            str = str.trim();
            String[] items = str.split("\t");
            if(items.length != len) {
                System.err.println("the matrix is wrong!!!");
            }

            for(int j = 0; j < items.length; ++j) {
                double v = (new Double(items[j].trim())).doubleValue();
                m.setEntry(i, j, v);
                if(v > 0.0D) {
                    ++num;
                }
            }
        }

        br.close();
        System.out.println("the number of nonzero entry is: " + num);
        return m;
    }

    public int length(String inputpath) throws Exception {
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String str = br.readLine().trim();
        String[] items = str.split("\t");
        System.out.println("the number of nodes in this graph: " + items.length);
        br.close();
        return items.length;
    }

    public RealMatrix read_vector(String inputpath) throws Exception {
        File file = new File(inputpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        ArrayList<Double> arr = new ArrayList();
        String str = null;

        while((str = br.readLine()) != null) {
            str = str.trim();
            if(str.length() == 0) {
                this.l = arr.size();
                System.out.println("the number of labelled nodes are: " + this.l);
            } else {
                Double v = new Double(str);
                arr.add(v);
            }
        }

        br.close();
        double[] value = new double[arr.size()];

        for(int i = 0; i < arr.size(); ++i) {
            value[i] = ((Double)arr.get(i)).doubleValue();
        }

        RealMatrix m = new Array2DRowRealMatrix(value);
        System.out.println("the number of nodes in initial ranking vector is: " + arr.size());
        return m;
    }

    public int[] diagonal_nonzeros(RealMatrix m) {
        int len = m.getRowDimension();
        ArrayList<Integer> arr = new ArrayList();

        for(int i = 0; i < len; ++i) {
            double sum = 0.0D;

            for(int j = 0; j < len; ++j) {
                sum += m.getEntry(i, j);
            }

            if(sum <= 0.0D) {
                System.err.println("000000000000000000");
            }

            if(sum > 0.0D) {
                arr.add(new Integer(i));
            }
        }

        int[] nonzeros = new int[arr.size()];
        System.out.println("len: " + len + " arr: " + arr.size());

        for(int i = 0; i < arr.size(); ++i) {
            nonzeros[i] = ((Integer)arr.get(i)).intValue();
        }

        return nonzeros;
    }

    public RealMatrix diagonal_matrix(RealMatrix m) {
        int len = m.getRowDimension();
        RealMatrix diag = new Array2DRowRealMatrix(len, len);

        for(int i = 0; i < len; ++i) {
            double sum = 0.0D;

            for(int j = 0; j < len; ++j) {
                diag.setEntry(i, j, 0.0D);
                sum += m.getEntry(i, j);
            }

            diag.setEntry(i, i, sum);
        }

        return diag;
    }

    public RealMatrix identity_matrix(int len) {
        RealMatrix m = new Array2DRowRealMatrix(len, len);

        for(int i = 0; i < len; ++i) {
            for(int j = 0; j < len; ++j) {
                m.setEntry(i, j, 0.0D);
            }

            m.setEntry(i, i, 1.0D);
        }

        return m;
    }

    public void set_zeros(RealMatrix m) {
        int len = m.getRowDimension();

        for(int i = 0; i < len; ++i) {
            for(int j = 0; j < len; ++j) {
                m.setEntry(i, j, 0.0D);
            }
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

    public void normalize_matrix(RealMatrix m) {
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
}
