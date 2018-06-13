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
    private int routerId;
    private ArrayList<String> localIps;
    private ArrayList<ProbeSet> probeSets;
    private ArrayList<ProbeSet> networkProbeSets;

    Router(P4TesterBDD bdd, String name, int routerId) {
        this.bdd = bdd;
        this.prefixes = new ArrayList<>();
        this.routerId = routerId;
        this.name = name;
        this.rules = new ArrayList<>();
        this.ports = new ArrayList<>();
        this.nextHops = new ArrayList<>();
        this.probes = new ArrayList<>();
        this.localIps = new ArrayList<>();
        this.probeSets = new ArrayList<>();
        this.networkProbeSets = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addIPv4withPrefix(String str, String port, String next_hop) {
        String[] ipv4 =  str.split("/");
        if (ipv4.length != 2) {
            return;
        }

        String[] ip = ipv4[0].split("\\.");
        int prefix = Integer.valueOf(ipv4[1]);

        ArrayList<Integer> bits = new ArrayList<>();
        System.out.println(ipv4[0]);
        System.out.flush();

        for (String i:ip) {
            int value = Integer.valueOf(i);
            int c = 128;
            for (int j = 0; j < 8; j++) {
                if (value >= c) {
                    bits.add(1);
                    value = value >> 1;
                } else {
                    bits.add(0);
                }

                c = c >> 1;
            }
        }
        int rule = this.bdd.getVar(0);

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


    public void generateProbeSets() {
        int[] prefixMap = new int[34];
        int[] ruleIds = new int[this.rules.size()];
        for (int i:this.prefixes) {
            prefixMap[i] ++;
        }

        for (int i = 32; i >= 1 ; i--) {
            prefixMap[i] += prefixMap[i+1];
        }

        for (int i = 0; i < this.rules.size(); i++) {
            prefixMap[i]--;
            ruleIds[prefixMap[i]] = i;
        }

        int Ha = bdd.getTrueBDD();


        for (int i = 0; i < ruleIds.length; i++) {
            int rid_i = ruleIds[i];
            int r = this.rules.get(rid_i);

            int rh = bdd.and(Ha, r);
            Ha = bdd.subtract(Ha, r);

            if (this.bdd.oneSAT(rh) != 0) {
                int Hb = rh;
                for (int j = i + 1; j < ruleIds.length; j++) {
                    int rid_j = ruleIds[j];
                    int override = this.bdd.and(Hb, this.rules.get(rid_j));

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

                if (this.bdd.oneSAT(Hb) != 0) {
                    ProbeSet probeSet = new ProbeSet(Hb);
                    probeSet.addRouter(this);
                    probeSet.addRule(r);
                    this.probeSets.add(probeSet);
                }
            }
        }


    }

    public void addNetworkProbeSets(ProbeSet probeSet) {
        this.networkProbeSets.add(probeSet);
    }

    public int getRouterId() {
        return this.routerId;
    }


    public void internal_test() {
        this.addIPv4withPrefix("10.0.0.0/8", "1", "2");
        this.addIPv4withPrefix("3.0.0.0/9", "3", "2");

        int n = this.bdd.and(this.rules.get(0), this.rules.get(1));
        this.bdd.print(this.rules.get(0));
        this.bdd.print(this.rules.get(1));
        this.bdd.print(n);
    }
}
