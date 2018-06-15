package org.p4tester;



import java.util.ArrayList;
import java.util.HashMap;


class RouterRule {
    private String matchIp;
    private int matchBdd;
    private int prefix;
    private String port;
    private String nextHop;
    private ArrayList<SwitchProbeSet> switchProbeSets;

    RouterRule(String matchIp, int matchBdd, int prefix, String port, String nextHop) {
        this.matchIp = matchIp;
        this.matchBdd = matchBdd;
        this.prefix = prefix;
        this.port = port;
        this.nextHop = nextHop;
        this.switchProbeSets = new ArrayList<>();
    }


    public ArrayList<SwitchProbeSet> getSwitchProbeSets() {
        return switchProbeSets;
    }

    public int getMatchBdd() {
        return matchBdd;
    }

    public String getMatchIp() {
        return matchIp;
    }

    public int getPrefix() {
        return prefix;
    }

    public String getPort() {
        return port;
    }

    public String getNextHop() {
        return nextHop;
    }

    public void addSwitchProbeSet(SwitchProbeSet probeSet) {
        this.switchProbeSets.add(probeSet);
    }
}

public class Router {
    private HashMap<String, RouterRule> ruleHashMap;
    private ArrayList<RouterRule> rules ;
    private ArrayList<Probe> probes;
    private P4TesterBDD bdd;
    private String name;
    private ArrayList<String> localIps;
    private ArrayList<SwitchProbeSet> switchProbeSets;
    private ArrayList<NetworkProbeSet> networkProbeSets;
    private static final int MAX_RULES = 20000;
    private boolean enablePriorityChecking;
    private BDDTree tree;
    private BDDTree complementTree;

    enum RULE_UPDATE_OPERATIONS {
        ADD_RULE,
        REMOVE_RULE
    }

    Router(P4TesterBDD bdd, String name) {
        this.bdd = bdd;
        this.name = name;
        this.rules = new ArrayList<>();
        this.probes = new ArrayList<>();
        this.localIps = new ArrayList<>();
        this.switchProbeSets = new ArrayList<>();
        this.networkProbeSets = new ArrayList<>();
        this.enablePriorityChecking = false;

        BDDTreeNode rootNode = new BDDTreeNode(new ProbeSet(this.bdd.getTrueBDD()), bdd);

        this.complementTree = new BDDTree(bdd, rootNode, null);

        ruleHashMap = new HashMap<>();
    }

    public BDDTree getTree() {
        return tree;
    }

    public void addComplementProbeSet(ProbeSet probeSet) {
        this.complementTree.insertProbeSet(probeSet);
    }

    public void buildTree() {
        this.tree = BDDTree.buildBinary(bdd, this.probeSets);
    }

    public ArrayList<RouterRule> getRules() {
        return rules;
    }

    public String getName() {
        return name;
    }

    public void addIPv4withPrefix(String str, String port, String nextHop) {
        String[] ipv4 =  str.split("/");

        if (ipv4.length != 2) {
            return;
        }

        if (this.rules.size() > MAX_RULES) {
            return;
        }

        String[] ip = ipv4[0].split("\\.");
        int prefix = Integer.valueOf(ipv4[1]);

        ArrayList<Integer> bits = new ArrayList<>();

        for (String i:ip) {
            int value = Integer.valueOf(i);
            for (int j = 0; j < 8; j++) {
                bits.add(value / (1 << (7 - j)));
                value = value % (1 << (7 - j));
            }
        }

        int rule;
        if (prefix > 0) {
            rule = this.bdd.getVar(0);

            if (bits.get(0) == 0) {
                rule = this.bdd.getNotVar(0);
            }

            for (int i = 1; i < 32; i++) {
                if (i < prefix) {
                    if (bits.get(i) == 1) {
                        int var = rule;
                        rule = this.bdd.and(rule, this.bdd.getVar(i));
                        this.bdd.deref(var);
                    } else {
                        int var = rule;
                        rule = this.bdd.and(rule, this.bdd.getNotVar(i));
                        this.bdd.deref(var);
                    }
                } else {
                    int var = this.bdd.or(this.bdd.getVar(i), this.bdd.getNotVar(i));
                    rule = this.bdd.and(rule, var);
                    this.bdd.deref(var);
                }
            }
        } else {
            rule = this.bdd.getTrueBDD();
        }
 //       System.out.println(str);
   //     this.bdd.print(rule);
        RouterRule routerRule = new RouterRule(str, rule, prefix, port, nextHop);
        this.rules.add(routerRule);
        this.ruleHashMap.put(str, routerRule);
    }

    public BDDTree getComplementTree() {
        return complementTree;
    }

    public void addProbe(Probe probe) {
        this.probes.add(probe);
    }

    public ArrayList<Probe> getProbes() {
        return probes;
    }

    public boolean hasLocalIp(String ip) {
        return this.localIps.contains(ip);
    }

    public void addLocalIp(String ip) {
        if (!this.localIps.contains(ip)) {
            this.localIps.add(ip);
        }
    }

    public void generateProbeSets() {
        int[] prefixMap = new int[34];
        int[] ruleIds = new int[this.rules.size()];
        for (RouterRule r:this.rules) {
            prefixMap[r.getPrefix()] ++;
        }

        for (int i = 31; i >= 0  ; i--) {
            prefixMap[i] += prefixMap[i + 1];
        }

        for (int i = 0; i < this.rules.size(); i++) {
            try {
                int var = -- prefixMap[rules.get(i).getPrefix()];
                ruleIds[var] = i;
            } catch (Exception e) {
                System.out.println(ruleIds.length);
                System.out.println(prefixMap[i]);
            }
        }
        int[] tmp_rules = new int[this.rules.size()];
        int Ha = bdd.getTrueBDD();
        for (int i = 0; i < ruleIds.length; i++) {
            int rid_i = ruleIds[i];
            int r = this.rules.get(rid_i).getMatchBdd();
            // System.out.println(prefixes.get(rid_i));
            tmp_rules[i] = bdd.and(r, Ha);
            Ha = bdd.subtract(Ha, tmp_rules[i]);
        }

        // int count = 0;
        for (int i = 0; i < ruleIds.length; i++) {
            RouterRule rule_i = rules.get(ruleIds[i]);
            int r = rule_i.getMatchBdd();
            // System.out.println(prefixes.get(rid_i));
            int rh = tmp_rules[i];
            // this.bdd.print(this.bdd.oneSAT(rh));
//            Ha = bdd.subtract(Ha, rh);
            if (this.bdd.oneSAT(rh) != 0) {
                int Hb = rh;
                if (this.enablePriorityChecking) {

                    for (int j = prefixMap[rule_i.getPrefix() + 1]; j < ruleIds.length; j++) {
                        RouterRule rule_j = rules.get(ruleIds[j]);
                        int override = this.bdd.and(Hb, tmp_rules[j]);

                        if (this.bdd.oneSAT(override) != 0) {
                            if (!rule_i.getPort().equals(rule_j.getPort())) {
                                SwitchProbeSet switchProbeSet = new SwitchProbeSet(rule_i, override, this);
                                rule_i.addSwitchProbeSet(switchProbeSet);
                                this.switchProbeSets.add(switchProbeSet);
                            }
                            Hb = this.bdd.subtract(Hb, override);
                        }
                    }
                }
                if (this.bdd.oneSAT(Hb) != 0) {
                    SwitchProbeSet switchProbeSet = new SwitchProbeSet(rule_i, Hb, this);
                    rule_i.addSwitchProbeSet(switchProbeSet);
                    this.switchProbeSets.add(switchProbeSet);
                }
            }
            //else {
                // count ++;
                // System.out.println(i);
            // }
        }
        // System.out.println(count);
    }

    public ArrayList<SwitchProbeSet> getSwitchProbeSets() {
        return this.switchProbeSets;
    }

    public void addNetworkProbeSets(NetworkProbeSet probeSet) {
        this.networkProbeSets.add(probeSet);
    }

    private ArrayList<ProbeSet> addRuleWithPriority(int rule, int prefix, int port) {
        return null;
    }


    private ArrayList<ProbeSet> addRuleWithoutPriority(String matchIp, int matchBdd, int prefix, String port, String nextPort) {
        RouterRule rule = new RouterRule(matchIp, matchBdd, prefix, port, nextPort);
        ProbeSet probeSet = null;
        if (this.bdd.isOverlap(matchBdd, this.tree.getRoot().getProbeSet().getExp())) {
            int  var = this.bdd.subtract(matchBdd, this.tree.getRoot().getProbeSet().getExp());
            if (this.bdd.oneSAT(var) != 0) {
                probeSet = new ProbeSet(var);
                this.tree.insertProbeSet(probeSet);
            }
        } else {
            probeSet = new ProbeSet(matchBdd);
        }

        rule.addProbeSet(probeSet);
        ArrayList<ProbeSet> probeSets = new ArrayList<>();

        ProbeSet networkProbeSet = this.complementTree.query(probeSet.getExp());

        if (networkProbeSet == null) {
            probeSet.addRouter(this);
            probeSets.add(probeSet);
        } else {
            networkProbeSet.addRouter(this);
            probeSets.add(networkProbeSet);
        }

        this.rules.add(rule);
        return probeSets;
    }

    private void removeRule(String match) {
        RouterRule rule = this.ruleHashMap.get(match);

        if (rule != null) {
            ArrayList<ProbeSet> probeSets = rule.getProbeSets();
            // TODO
        }
    }

    public void updateRule(String match, String port, String nextHop, RULE_UPDATE_OPERATIONS op) {

        String[] ipv4 =  match.split("/");
        if (ipv4.length != 2) {
            return;
        }

        if (this.rules.size() > MAX_RULES) {
            return;
        }

        String[] ip = ipv4[0].split("\\.");
        int prefix = Integer.valueOf(ipv4[1]);

        ArrayList<Integer> bits = new ArrayList<>();

        for (String i:ip) {
            int value = Integer.valueOf(i);
            for (int j = 0; j < 8; j++) {
                bits.add(value / (1 << (7 - j)));
                value = value % (1 << (7 - j));
            }
        }

        int rule;
        if (prefix > 0) {
            rule = this.bdd.getVar(0);

            if (bits.get(0) == 0) {
                rule = this.bdd.getNotVar(0);
            }

            for (int i = 1; i < 32; i++) {
                if (i < prefix) {
                    int var = rule;
                    if (bits.get(i) == 1) {
                        rule = this.bdd.and(rule, this.bdd.getVar(i));
                    } else {
                        rule = this.bdd.and(rule, this.bdd.getNotVar(i));
                    }
                    this.bdd.deref(var);
                } else {
                    int var = this.bdd.or(this.bdd.getVar(i), this.bdd.getNotVar(i));
                    rule = this.bdd.and(rule, var);
                }
            }
        } else {
            rule = this.bdd.getTrueBDD();
        }

        switch (op) {
            case ADD_RULE:
                this.addRuleWithoutPriority(match, rule, prefix, port, nextHop);
                break;
            case REMOVE_RULE:
                this.removeRule(match);
                break;
        }
    }

    public ArrayList<NetworkProbeSet> getNetworkProbeSets() {
        return networkProbeSets;
    }

    public void internalTest() {
        this.addIPv4withPrefix("169.154.229.160/27", "1", "2");
        this.addIPv4withPrefix("1.0.0.0/8", "3", "2");

        // int n = this.bdd.subtract(this.rules.get(1), this.rules.get(0));

        // this.bdd.print(this.rules.get(0));
    }
}
