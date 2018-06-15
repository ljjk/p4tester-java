package org.p4tester;

import java.util.ArrayList;

public class NetworkProbeSet extends ProbeSet {
    private ArrayList<Router> routers;
    private ArrayList<SwitchProbeSet> switchProbeSets;
    private ArrayList<Integer> paths;
    private ArrayList<Integer> recordRouters;
    private ArrayList<String> visitedRouters;
    private static final int MAX = 9;
    private int count;
    private P4TesterBDD bdd;

    NetworkProbeSet(P4TesterBDD bdd) {
        super(-1);
        this.bdd = bdd;
        this.switchProbeSets = new ArrayList<>();
        this.paths = new ArrayList<>();
        this.recordRouters = new ArrayList<>();
        this.visitedRouters = new ArrayList<>();
        this.routers = new ArrayList<>();
        this.count = 0;
    }

    @Override
    public int getMatch() {
        return match;
    }

    public void addSwitchProbeSet(SwitchProbeSet switchProbeSet) {
        this.switchProbeSets.add(switchProbeSet);
        this.addRouter(switchProbeSet.getRouter());

        if (this.match == -1) {
            this.match = switchProbeSet.getMatch();
        } else {
            int var = this.match;
            this.match = this.bdd.and(this.match, switchProbeSet.getMatch());
            this.bdd.deref(var);
        }
    }

    public ArrayList<SwitchProbeSet> getSwitchProbeSet() {
        return switchProbeSets;
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

    public void addRouter(Router router) {
        if (!routers.contains(router)) {
            this.routers.add(router);
        }
    }

    public ArrayList<Router> getRouters() {
        return routers;
    }


}
