package org.p4tester;

import java.util.ArrayList;

class BDDTreeNode {
    private ProbeSet probeSet;
    private int complement;
    private P4TesterBDD bdd;
    private BDDTreeNode parent;
    private ArrayList<Router> routers;
    private ArrayList<BDDTreeNode> children;
    private int leafNum;
    private int visitedNum;

    BDDTreeNode (ProbeSet probeSet, P4TesterBDD bdd, int leaf) {
        this.probeSet = probeSet;
        this.bdd = bdd;
        this.complement = probeSet.getExp();
        this.routers = new ArrayList<>();
        this.children = new ArrayList<>();
        this.parent = null;
        this.visitedNum = 0;
        this.leafNum = leaf;
    }

    BDDTreeNode (ProbeSet probeSet, P4TesterBDD bdd) {
        this.probeSet = probeSet;
        this.bdd = bdd;
        this.complement = probeSet.getExp();
        this.routers = new ArrayList<>();
        this.children = new ArrayList<>();
        this.parent = null;
        this.visitedNum = 0;
        this.leafNum = 0;
    }
    public boolean isVisited(Router name) {
        return routers.contains(name);
    }

    public void visit() {
        this.visitedNum = 1;
    }

    public BDDTreeNode getParent() {
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
        // this.addRouters(node.getRouters());
        node.addRouters(this.getRouters());
        // this.complement = bdd.and(complement, bdd.not(node.probeSet.getExp()));
        node.parent = this;
        this.leafNum += node.leafNum;
    }

    public void addRouter(Router router) {
        if (!routers.contains(router)) {
            this.routers.add(router);
        }
    }

    public int getLeafNum() {
        return leafNum;
    }

    public int getVisitedNum() {
        return visitedNum;
    }

    public void addRouters(ArrayList<Router> routers) {
        for(Router router: routers) {
            this.addRouter(router);
        }
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

    BDDTree (P4TesterBDD bdd, BDDTreeNode root) {
        this.root = root;
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
                if (!c.isVisited(probeSet.getRouters().get(0))) {
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
            }
            if (enableComplement) {
            //                if (bdd.isOverlap(node.getComplement(), probeSet.getExp())) {
                    ProbeSet tmp = new ProbeSet(bdd.and(node.getProbeSet().getExp(), probeSet.getExp()));
                    BDDTreeNode tmpNode = new BDDTreeNode(tmp, bdd);
                    tmpNode.addRouters(probeSet.getRouters());
                    node.addChild(tmpNode);
                    nodes.add(tmpNode);
            //              }
            }
        }
    }

    public ProbeSet query(int target) {
        BDDTreeNode node = root;
        boolean found = true;

        if (!bdd.isOverlap(root.getProbeSet().getExp(), target)) {
            return null;
        }

        while (!node.isLeaf() && found) {
            found = false;
            BDDTreeNode child = node.getChildren().get(0);
            if (child.getLeafNum() > child.getVisitedNum()) {
                if (bdd.isOverlap(child.getProbeSet().getExp(), target)) {
                    node.visit();
                    node = child;
                    found = true;
                }
            }
            if (!found) {
                child = node.getChildren().get(1);
                if (child.getLeafNum() > child.getVisitedNum()) {
                    node.visit();
                    node = child;
                    found = true;
                }
            }
        }
        if (found) {
            return node.getProbeSet();
        } else {
            return null;
        }
    }

    public ArrayList<ProbeSet> getLeafNodes() {

        ArrayList<ProbeSet> probeSets = new ArrayList<>();
        ArrayList<BDDTreeNode> nodes = new ArrayList<>();

        for(BDDTreeNode node: this.nodes) {
            if (node.isLeaf()) {
                ProbeSet probeSet = new ProbeSet(node.getProbeSet().getExp());

                BDDTreeNode tmp = node;

                while(tmp != null && !nodes.contains(tmp.getParent())) {
                    probeSet.addRouters(tmp.getRouters());
                    nodes.add(tmp);
                    // System.out.println(tmp.getRouters().size());
                    tmp = tmp.getParent();
                }

                probeSet.addRouters(node.getRouters());

                for (Router r: probeSet.getRouters()) {
                    r.addNetworkProbeSets(probeSet);
                }
                probeSets.add(probeSet);
            }
        }

        return  probeSets;
    }

    static public BDDTree buildBinary(P4TesterBDD bdd, ArrayList<ProbeSet> probeSets) {
        BDDTreeNode[] nodes = new BDDTreeNode[probeSets.size()];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new BDDTreeNode(probeSets.get(i), bdd, 1);
        }

        int len = nodes.length;
        int count;
        while(len > 1) {
            count = 0;
            for (int i = 0; i < len - 1; i += 2) {
                ProbeSet probeSet = new ProbeSet(bdd.or(nodes[i].getProbeSet().getExp(), nodes[i + 1].getProbeSet().getExp()));
                BDDTreeNode node = new BDDTreeNode(probeSet, bdd, 0);
                node.addChild(nodes[i]);
                node.addChild(nodes[i + 1]);
                nodes[count] = node;
                count ++;
            }
            if (len % 2 == 1) {
                nodes[count++] = nodes[len - 1];
            }
            // System.out.println(len);
            len = count;
        }
        return new BDDTree(bdd, nodes[0]);
    }
}
