package org.p4tester;



import java.util.ArrayList;
import java.util.Vector;

public class Router {
    private ArrayList<Integer> rules ;
    private ArrayList<Integer> prefixes;
    private ArrayList<String> ports;
    private ArrayList<String> nextHops;
    private ArrayList<Probe> probes;
    private P4TesterBDD bdd;
    private String name;
    private ArrayList<String> matches;
    private ArrayList<String> localIps;
    private ArrayList<ProbeSet> probeSets;
    private ArrayList<ProbeSet> networkProbeSets;
    private static final int MAX_RULES = 20000;
    private boolean enablePriorityChecking;
    private BDDTree tree;

    enum RULE_UPDATE_OPERATIONS {
        ADD_RULE,
        REMOVE_RULE
    }

    Router(P4TesterBDD bdd, String name) {
        this.bdd = bdd;
        this.prefixes = new ArrayList<>();
        this.name = name;
        this.rules = new ArrayList<>();
        this.ports = new ArrayList<>();
        this.nextHops = new ArrayList<>();
        this.probes = new ArrayList<>();
        this.localIps = new ArrayList<>();
        this.probeSets = new ArrayList<>();
        this.networkProbeSets = new ArrayList<>();
        this.matches = new ArrayList<>();
        this.enablePriorityChecking = false;
    }

    public BDDTree getTree() {
        return tree;
    }

    public void buildTree() {
        this.tree = BDDTree.buildBinary(bdd, this.probeSets);
    }

    public ArrayList<Integer> getRules() {
        return rules;
    }

    public String getName() {
        return name;
    }

    public void addIPv4withPrefix(String str, String port, String next_hop) {
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
        this.matches.add(str);

        int rule;
        if (prefix > 0) {
            rule = this.bdd.getVar(0);

            if (bits.get(0) == 0) {
                rule = this.bdd.getNotVar(0);
            }

            for (int i = 1; i < 32; i++) {
                if (i < prefix) {
                    if (bits.get(i) == 1) {
                        rule = this.bdd.and(rule, this.bdd.getVar(i));
                    } else {
                        rule = this.bdd.and(rule, this.bdd.getNotVar(i));
                    }
                } else {
                    int var = this.bdd.or(this.bdd.getVar(i), this.bdd.getNotVar(i));
                    rule = this.bdd.and(rule, var);
                }
            }
        } else {
            rule = this.bdd.getTrueBDD();
        }
 //       System.out.println(str);
   //     this.bdd.print(rule);
        this.prefixes.add(prefix);
        this.rules.add(rule);
        this.ports.add(port);
        this.nextHops.add(next_hop);
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
        for (int i:this.prefixes) {
            prefixMap[i] ++;
        }

        for (int i = 31; i >= 0  ; i--) {
            prefixMap[i] += prefixMap[i + 1];
        }

        for (int i = 0; i < this.rules.size(); i++) {
            try {
                int var = -- prefixMap[this.prefixes.get(i)];
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
            int r = this.rules.get(rid_i);
            // System.out.println(prefixes.get(rid_i));
            tmp_rules[i] = bdd.and(r, Ha);
            Ha = bdd.subtract(Ha, tmp_rules[i]);
        }

        // int count = 0;
        for (int i = 0; i < ruleIds.length; i++) {
            int rid_i = ruleIds[i];
            int r = this.rules.get(rid_i);
            // System.out.println(prefixes.get(rid_i));
            int rh = tmp_rules[i];
            // this.bdd.print(this.bdd.oneSAT(rh));
//            Ha = bdd.subtract(Ha, rh);
            if (this.bdd.oneSAT(rh) != 0) {
                int Hb = rh;
                if (this.enablePriorityChecking) {

                    for (int j = i + 1; j < ruleIds.length; j++) {
                        int rid_j = ruleIds[j];
                        int override = this.bdd.and(Hb, tmp_rules[j]);

                        if (this.bdd.oneSAT(override) != 0) {
                            if (!ports.get(rid_i).equals(ports.get(rid_j))) {
                                ProbeSet probeSet = new ProbeSet(override);
                                probeSet.addRule(r);
                                probeSet.addRouter(this);
                                this.probeSets.add(probeSet);
                            }
                            Hb = this.bdd.subtract(Hb, override);
                        }
                    }
                }
                if (this.bdd.oneSAT(Hb) != 0) {
                    ProbeSet probeSet = new ProbeSet(Hb);
                    probeSet.addRouter(this);
                    probeSet.addRule(r);
                    this.probeSets.add(probeSet);
                }
            }
            //else {
                // count ++;
                // System.out.println(i);
            // }
        }
        // System.out.println(count);
    }

    public ArrayList<ProbeSet> getProbeSets() {
        return probeSets;
    }

    public void addNetworkProbeSets(ProbeSet probeSet) {
        this.networkProbeSets.add(probeSet);
    }


    public void updateRule(String match, int port, RULE_UPDATE_OPERATIONS op) {
        
    }


    public ArrayList<ProbeSet> getNetworkProbeSets() {
        return networkProbeSets;
    }

    public void internalTest() {
        this.addIPv4withPrefix("169.154.229.160/27", "1", "2");
        this.addIPv4withPrefix("1.0.0.0/8", "3", "2");

        int n = this.bdd.subtract(this.rules.get(1), this.rules.get(0));

        this.bdd.print(this.rules.get(0));
    }
}
