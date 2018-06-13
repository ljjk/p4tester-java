package org.p4tester;


public class Main {

    public static void main(String[] args) {
        P4TesterBDD bdd = new P4TesterBDD(32);

        P4Tester p4tester = new P4Tester(bdd);
        long start = System.nanoTime();
        p4tester.probeConstruct();
        System.out.println(System.nanoTime() - start);

        start = System.nanoTime();
        p4tester.buildBDDTreeFast();
        System.out.println(System.nanoTime() - start);

        start = System.nanoTime();
        p4tester.generateProbes();
        System.out.println(System.nanoTime() - start);
    }
}
