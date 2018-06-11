package org.p4tester;

import java.util.ArrayList;

public class ProbeSet {
    private ArrayList<Integer> rules;
    private ArrayList<Router> routers;
    private ArrayList<Integer> paths;

    private int exp;

    ProbeSet(int rule) {
        this.exp = rule;
        this.rules = new ArrayList<>();
        this.routers = new ArrayList<>();
        this.paths = new ArrayList<>();
    }


    public void addRule(int rule) {
        this.rules.add(rule);
    }

    public ArrayList<Integer> getRules() {
        return rules;
    }

    public ArrayList<Router> getRouters() {
        return routers;
    }

    public void addRouter(Router router) {
        if (!routers.contains(router)) {
            this.routers.add(router);
        }
    }

    public void addRouters(ArrayList<Router> routers) {
        for(Router router: routers) {
            this.addRouter(router);
        }
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }
}
