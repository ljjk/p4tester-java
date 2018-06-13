package org.p4tester;


public class Main {

    public static void main(String[] args) {
        P4TesterBDD bdd = new P4TesterBDD(32);

        Router router = new Router(bdd, "hous", 1);
        router.internal_test();

        P4Tester p4tester = new P4Tester(bdd);
        p4tester.internal_test();
    }
}
