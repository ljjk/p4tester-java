package org.p4tester;

import java.util.ArrayList;

class BDDTreeNode {
    private ProbeSet probeSet;
    private int complement;
    private P4TesterBDD bdd;
    private BDDTreeNode parent;
    private ArrayList<Router> routers;
    private ArrayList<BDDTreeNode> children;

    BDDTreeNode (ProbeSet probeSet, P4TesterBDD bdd) {
        this.probeSet = probeSet;
        this.bdd = bdd;
        this.complement = probeSet.getExp();
        this.routers = probeSet.getRouters();
        this.children = new ArrayList<>();
        this.parent = null;
    }

    public void setParanet(BDDTreeNode paranet) {
        this.parent = paranet;
    }

    public BDDTreeNode getParanet() {
        return parent;
    }

    public int getComplement() {
        return complement;
    }

    public ProbeSet getProbeSet() {
        return probeSet;
    }

    public void addChild(BDDTreeNode node) {
        this.children.add(node);
        node.addRouters(node.getRouters());
        this.complement = bdd.and(complement, bdd.not(node.complement));
        node.parent = this;
    }

    public void addRouter(Router router) {
        if (!routers.contains(router)) {
            this.routers.add(router);
        }
    }

    public void addRouters(ArrayList<Router> routers) {
        for(Router router: routers)
            this.addRouter(router);
    }

    public ArrayList<BDDTreeNode> getChildren() {
        return children;
    }


    public ArrayList<Router> getRouters() {
        return routers;
    }

    public boolean isLeaf() {
        return this.children.size() == 0;
    }
}

public class BDDTree {
    private BDDTreeNode root;
    private P4TesterBDD bdd;
    private ArrayList<BDDTreeNode> nodes;

    BDDTree (P4TesterBDD bdd) {
        root = new BDDTreeNode(new ProbeSet(bdd.getTrueBDD()), bdd);
        this.bdd = bdd;
        this.nodes = new ArrayList<>();
    }

    void insert(ProbeSet probeSet) {
        BDDTreeNode node = root;
        boolean whileContinue = true;
        boolean enableComplement;
        while(whileContinue) {
            whileContinue = false;
            enableComplement = true;
            for (BDDTreeNode c: node.getChildren()) {
                if (bdd.isOverlap(c.getProbeSet().getExp(), probeSet.getExp())) {
                    enableComplement = false;
                    if (bdd.isSubset(c.getProbeSet().getExp(), probeSet.getExp())) {
                        c.addRouters(probeSet.getRouters());
                        break;
                    } else {
                        c.addRouters(probeSet.getRouters());
                        node = c;
                        whileContinue = true;
                        break;
                    }
                }
            }
            if (enableComplement) {
                if (bdd.isOverlap(node.getComplement(), probeSet.getExp())) {
                    ProbeSet tmp = new ProbeSet(bdd.and(node.getProbeSet().getExp(), probeSet.getExp()));
                    BDDTreeNode tmpNode = new BDDTreeNode(tmp, bdd);
                    node.addChild(tmpNode);
                }
            }
        }
    }


    public ArrayList<ProbeSet> getLeafNodes() {

        ArrayList<ProbeSet> probeSets = new ArrayList<>();
        ArrayList<BDDTreeNode> nodes = new ArrayList<>();
        for(BDDTreeNode node: this.nodes) {
            if (node.isLeaf()) {
                ProbeSet probeSet = new ProbeSet(node.getProbeSet().getExp());

                BDDTreeNode tmp = node;

                while(!nodes.contains(tmp.getParanet())) {
                    probeSet.addRouters(tmp.getRouters());
                    nodes.add(tmp);
                    tmp = tmp.getParanet();
                }

                for (Router r: probeSet.getRouters()) {
                    r.addNetworkProbeSets(probeSet);
                }

                probeSets.add(probeSet);
            }
        }

        return  probeSets;
    }
}
