package com.thirdparty.morph.identification.libsvm;

import java.io.Serializable;

/**
 * Created by helencoder on 2018/1/4.
 */
public class svm_problem implements Serializable {
    public int l;
    public double[] y;
    public svm_node[][] x;
}
