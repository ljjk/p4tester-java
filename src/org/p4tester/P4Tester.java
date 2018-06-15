package org.p4tester;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


class SwitchPortPair {
    private Router router;
    private short port;

    SwitchPortPair(Router router, short port) {
        this.router = router;
        this.port = port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    public short getPort() {
        return port;
    }

    public Router getRouter() {
        return router;
    }
}

public class P4Tester {
    private ArrayList<Router> routers;
    private ArrayList<NetworkProbeSet> probeSets;
    private P4TesterBDD bdd;
    private BDDTree tree;
    private HashMap<String, Router> routerMap;
    private HashMap<String, ArrayList<Router>> topoMap;
    private Router root;
    private ArrayList<SwitchPortPair> path;
    private static final String[] INTERNET2_ROUTERS = {
            "atla",
            "chic",
            "hous",
            "kans",
            "losa",
            "newy32aoa",
            "salt",
            "wash",
            "seat"
    };

    private static final String[] STANFORD_CITIES = {
            "atla",
            "chic",
            "hous",
            "kans",
            "losa",
            "newy32aoa",
            "salt",
            "wash"
    };

    P4Tester(P4TesterBDD bdd) {
        this.bdd = bdd;
        this.routerMap = new HashMap<>();
        this.probeSets = new ArrayList<>();
        this.tree = new BDDTree(bdd);
        this.routers = new ArrayList<>();
        this.topoMap = new HashMap<>();
        this.path = new ArrayList<>();

    }

    public void encodeInternet2(String routerName, String fileName) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(fileName));
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;
            Router router = new Router(bdd, routerName);

            for (int i =0 ;i < 7; i++) {
                reader.readLine();
            }
            String match;
            String port;
            String nextHop;
            while((line = reader.readLine()) != null) {
                match = null;
                port = null;
                nextHop = null;

                if (line.contains(":")) {
                    continue;
                }

                try {
                    if (line.contains("indr")) {
                        String[] info = line.split(" ");

                        for (String i : info) {
                            if (i.contains("/")) {
                                match = i;
                                break;
                            }
                        }
                        line = reader.readLine();
                        info = line.split(" ");

                        for (String i : info) {
                            if (nextHop == null) {
                                if (i.contains(".")) {
                                    nextHop = i;
                                }
                            }
                            port = i;
                        }
                        router.addIPv4withPrefix(match, port, nextHop);
                    } else if (line.contains("ucst")) {
                        String[] info = line.split(" ");

                        for (String i : info) {
                            if (match == null) {
                                if (i.contains("/")) {
                                    match = i;
                                }
                            } else if (nextHop == null) {
                                if (i.contains(".")) {
                                    nextHop = i;
                                }
                            }
                            port = i;
                        }

                        if (match != null) {
                            // router.addIPv4withPrefix(match, port, nextHop);
                        }
                    } else if (line.contains("locl")) {
                        String[] info = line.split(" ");

                        for (String i : info) {
                            if (match == null) {
                                if (i.contains("/")) {
                                    match = i;
                                }
                            } else if (i.contains(".")) {
                                nextHop = i;
                                break;
                            }
                        }
                        router.addLocalIp(nextHop);
                    }
                } catch (Exception e) {

                }
            }
            router.generateProbeSets();
            this.routers.add(router);
            routerMap.put(routerName, router);
            inputStreamReader.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO
    public void encodeStanford(String router_name, String fileName) {

    }

    public ArrayList<Short> getForwardPortList(String name) {
        return new ArrayList<>();
    }

    public ArrayList<Short> getBackwordPortList(String name) {
        return new ArrayList<>();
    }

    @Deprecated
    public void buildBDDTree() {
        // Router router = this.routers.get(0);
        /*
        for (Router router:this.routers) {
            for (ProbeSet probeSet : router.getProbeSets()) {
                tree.insert(probeSet);
            }
        }
        */
        //this.probeSets = tree.getLeafNodes();
    }

    public void buildBDDTreeFast() {
        // Router router = this.routers.get(0);

        HashSet<SwitchProbeSet> visitedProbeSets = new HashSet<>();
        for (int i = 0; i < routers.size(); i++) {
            ArrayList<SwitchProbeSet> probeSets = routers.get(i).getSwitchProbeSets();

            for (int j = 0; j < probeSets.size(); j++) {
                if (!visitedProbeSets.contains(probeSets.get(j))) {
                    visitedProbeSets.add(probeSets.get(j));
                    int target = probeSets.get(j).getMatch();

                    NetworkProbeSet networkProbeSet = new NetworkProbeSet(bdd, this);
                    networkProbeSet.addSwitchProbeSet(probeSets.get(j));

                    for (int k = 0; k < routers.size(); k++) {
                        if (k != i) {
                            BDDTreeNode treeNode = routers.get(k).getTree().query(target);
                            if (treeNode != null) {
                                visitedProbeSets.add(treeNode.getSwitchProbeSet());
                                networkProbeSet.addSwitchProbeSet(treeNode.getSwitchProbeSet());
                                target = networkProbeSet.getMatch();
                            } else {
                                // routers.get(k).getComplementTree().insertNetworkProbeSet(networkProbeSet);
                            }
                        }
                    }

                    this.probeSets.add(networkProbeSet);
                }
            }
        }

        System.out.println(this.probeSets.size());
        // int count = 0;
        // for (Router router : this.routers) {
            // count += router.getProbeSets().size();
            // System.out.println(count);
        //}
        // this.probeSets = tree.getLeafNodes();
    }

    private void buildInternet2ST() {
        ArrayList<Router> children = new ArrayList<>();
        children.add(this.routers.get(1));
        children.add(this.routers.get(2));
        children.add(this.routers.get(3));

        this.topoMap.put(INTERNET2_ROUTERS[0], children);

        children = new ArrayList<>();
        children.add(this.routers.get(4));
        children.add(this.routers.get(5));

        this.topoMap.put(INTERNET2_ROUTERS[1], children);

        children = new ArrayList<>();
        children.add(this.routers.get(6));
        children.add(this.routers.get(7));

        this.topoMap.put(INTERNET2_ROUTERS[2], children);

        children = new ArrayList<>();
        children.add(this.routers.get(8));

        this.topoMap.put(INTERNET2_ROUTERS[5], children);

        this.root = this.routers.get(0);
    }

    public ArrayList<SwitchPortPair> getPath() {
        return path;
    }

    public void generateProbes() {
        this.buildInternet2ST();

        traverseST(root);
    }

    private void traverseST(Router router) {
        for (NetworkProbeSet probeSet:router.getNetworkProbeSets()) {
                probeSet.traverse(router, this.path.size());
        }

        if (this.topoMap.containsKey(router.getName())) {
            for (Router child: this.topoMap.get(router.getName())) {
                path.add(new SwitchPortPair(child, router.getPort(child.getName())));
                traverseST(child);
                path.add(new SwitchPortPair(router, child.getPort(router.getName())));
            }
        }
    }

    public void probeConstruct() {
        ArrayList<Thread> constructors = new ArrayList<>();
        for (String s: INTERNET2_ROUTERS) {
            String fileName = "resource/Internet2/" +  s + "-show_route_forwarding-table_table_default.xml";
            Thread t = new Thread(new P4TesterProbeSetConstructor(this, s, fileName));
            t.run();
            // constructors.add(t);
        }
        for (Thread constructor: constructors) {
            constructor.start();
        }

        for (Thread constructor: constructors) {
            try {
                constructor.join();
            }
            catch (Exception e) {
                System.out.println("JOIN");
                e.printStackTrace();
            }
        }
        for (Router router:this.routers) {
            router.buildTree();
        }
    }

    public void internalTest() {
        this.encodeInternet2("", "resource/Internet2/hous-show_route_forwarding-table_table_default.xml");
    }


    public void removeRule(String routerName, String match) {
        Router router = this.routerMap.get(routerName);
        if (router != null) {
            ArrayList<NetworkProbeSet> networkProbeSets = router.removeRule(match);
            for (NetworkProbeSet networkProbeSet: networkProbeSets) {

            }
        }
    }


    public void addRule(String match, String port, String nexthop) {

    }
}

class P4TesterProbeSetConstructor implements Runnable {
    private P4Tester p4tester;
    private String router;
    private String fileName;

    P4TesterProbeSetConstructor(P4Tester tester, String router, String fileName) {
        this.router = router;
        this.p4tester = tester;
        this.fileName = fileName;
    }
    @Override
    public void run() {
        this.p4tester.encodeInternet2(router, fileName);
    }
}