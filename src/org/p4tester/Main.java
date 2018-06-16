package org.p4tester;


public class Main {

    public static void main(String[] args) {
        P4TesterBDD bdd = new P4TesterBDD(32);


        P4Tester p4tester = new P4Tester(bdd);
        p4tester.internalTest();
        // p4tester.start();
    }
}
