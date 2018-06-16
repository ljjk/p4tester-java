package org.p4tester;

public class SwitchProbeSet extends ProbeSet {
    private NetworkProbeSet networkProbeSet;
    private Router router;
    private RouterRule routerRule;
    private BDDTreeNode node;
    private int overridePriority;

    SwitchProbeSet(RouterRule rule, int match, Router router, int overridePriority) {
        super(match);
        this.routerRule = rule;
        this.router = router;
        this.networkProbeSet = null;
        this.node = null;
        this.overridePriority = overridePriority;
    }

    public Router getRouter() {
        return router;
    }

    public void setNode(BDDTreeNode node) {
        this.node = node;
    }

    public BDDTreeNode getNode() {
        return node;
    }

    public void setNetworkProbeSet(NetworkProbeSet networkProbeSet) {
        this.networkProbeSet = networkProbeSet;
    }

    public NetworkProbeSet getNetworkProbeSet() {
        return networkProbeSet;
    }

    public RouterRule getRouterRule() {
        return routerRule;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
