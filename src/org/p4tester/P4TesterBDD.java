package org.p4tester;

import jdd.bdd.*;

import java.util.ArrayList;


public class P4TesterBDD {


    private ArrayList<Integer> vars;
    private ArrayList<Integer> notVars;
    private int n;
    private BDD bdd;
    private int trueBDD;
    private int lock;

    P4TesterBDD(int n) {
        this.lock = 0;
        this.vars = new ArrayList<>();
        this.notVars = new ArrayList<>();
        this.n = n;
        bdd = new BDD(20000000, 1000000);
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

    public void tryLock() {
        // while(this.lock == 1);
        this.lock = 1;
    }

    public void releaseLock() {
        this.lock = 0;
    }

    public int getVar(int i) {
        return vars.get(i);
    }

    public int getNotVar(int i) {
        return notVars.get(i);
    }

    public boolean isOverlap(int a, int b) {
        this.tryLock();
        int var = bdd.and(a, b);
        boolean sat =  bdd.oneSat(var) != 0;
        this.bdd.deref(var);
        this.releaseLock();
        return sat;
    }

    public boolean isSubset(int a, int b) {
        this.tryLock();
        int var1 = bdd.not(b);
        int var2 = bdd.and(a, var1);
        boolean sat = (bdd.oneSat(var2) == 0);
        this.bdd.deref(var1);
        this.bdd.deref(var2);
        this.releaseLock();
        return sat;
    }

    public int or (int a, int b) {
        this.tryLock();
        int var = bdd.or(a, b);
        this.releaseLock();
        return var;
    }

    public int and (int a, int b) {
        this.tryLock();
        int var = bdd.and(a, b);
        this.releaseLock();
        return var;
    }

    public int not (int a) {
        this.tryLock();
        int var = bdd.not(a);
        this.releaseLock();
        return var;
    }

    public int oneSAT(int a) {
        this.tryLock();
        int var = bdd.oneSat(a);
        this.releaseLock();
        return var;
    }

    public int subtract (int a, int b) {
        this.tryLock();
        int var1 = bdd.not(b);
        int var2 = bdd.and(a, var1);
        this.bdd.deref(var1);
        this.releaseLock();
        return var2;
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

    public void deref(int n) {
        this.bdd.deref(n);
    }

}
