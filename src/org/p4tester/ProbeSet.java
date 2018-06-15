package org.p4tester;

import java.util.ArrayList;

public class ProbeSet {
    private ArrayList<Integer> rules;
    private ArrayList<Router> routers;
    private ArrayList<Integer> paths;
    private ArrayList<Integer> recordRouters;
    private ArrayList<String> visitedRouters;
    private int count;
    private int exp;
    private int priority;
    private static final int MAX = 9;

    ProbeSet(int rule) {
        this.exp = rule;
        this.rules = new ArrayList<>();
        this.routers = new ArrayList<>();
        this.paths = new ArrayList<>();
        this.visitedRouters = new ArrayList<>();
        this.recordRouters = new ArrayList<>();
        this.count = 0;
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

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public void traverse(Router router, int i) {
        if (!this.visitedRouters.contains(router.getName())) {
            this.visitedRouters.add(router.getName());
            this.recordRouters.add(i);

            if (this.count == 0) {
                this.paths.add(i);
            }
            this.count++;
            if (this.count == MAX) {
                this.paths.add(i);
                this.count = 0;
            }

            if (this.recordRouters.size() == this.getRouters().size()) {
                this.paths.add(i);
            }

        }
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }
}
