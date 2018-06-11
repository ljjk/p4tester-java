package org.p4tester;

import jdd.bdd.*;

import java.util.ArrayList;


public class P4TesterBDD {


    private ArrayList<Integer> vars;
    private ArrayList<Integer> notVars;
    private int n;
    private BDD bdd;
    private int trueBDD;

    P4TesterBDD(int n) {
        this.vars = new ArrayList<>();
        this.notVars = new ArrayList<>();
        this.n = n;
        bdd = new BDD(1000, 1000);
        for (int i = 0; i < n; i++) {
            int var = bdd.createVar();
            vars.add(var);
        }
        for (int i = 0 ;i < n; i++) {
            notVars.add(bdd.not(vars.get(i)));
        }
        this.trueBDD = bdd.or(vars.get(0), notVars.get(0));
        for (int i = 1; i < n ; i++) {
            int tmp = bdd.or(vars.get(0), notVars.get(0));
            this.trueBDD = bdd.and(this.trueBDD, tmp);
        }
    }

    public int getVar(int i) {
        return vars.get(i);
    }

    public int getNotVar(int i) {
        return notVars.get(i);
    }

    public boolean isOverlap(int a, int b) {
        int var = bdd.and(a, b);
        return bdd.oneSat(var) != 0;
    }

    public boolean isSubset(int a, int b) {
        int var = bdd.not(b);
        var = bdd.and(a, var);
        return  bdd.oneSat(var) == 0;
    }

    public int or (int a, int b) {
        return bdd.or(a, b);
    }

    public int and (int a, int b) {
        return bdd.and(a, b);
    }

    public int not (int a) {
        return bdd.not(a);
    }

    public int oneSAT(int a) {
        return bdd.oneSat(a);
    }

    public int subtract (int a, int b) {
        int var = bdd.not(b);
        return bdd.and(a, var);
    }

    public int getN() {
        return n;
    }

    public int getTrueBDD() {
        return trueBDD;
    }

    public void print(int n) {
        this.bdd.printSet(n);
    }
}
