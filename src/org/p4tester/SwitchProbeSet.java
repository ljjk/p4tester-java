package org.p4tester;

public class SwitchProbeSet extends ProbeSet {
    private NetworkProbeSet networkProbeSet;
    private Router router;
    private RouterRule routerRule;

    SwitchProbeSet(RouterRule rule, int match, Router router) {
        super(match);
        this.routerRule = rule;
        this.router = router;
        this.networkProbeSet = null;
    }

    public Router getRouter() {
        return router;
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
