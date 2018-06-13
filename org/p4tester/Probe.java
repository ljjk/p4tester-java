package org.p4tester;

import java.util.ArrayList;

public class Probe {
    private ArrayList<Integer> headerBits;
    private ArrayList<Integer> routers;
    private ArrayList<Integer> ports;


    public Probe() {
        this.headerBits = null;
        this.routers = new ArrayList<>();
        this.ports = new ArrayList<>();
    }

    public void setHeaderBits(ArrayList<Integer> header_bits) {
        this.headerBits = header_bits;
    }

    public void addRouterPort(int router, int port) {
        this.routers.add(router);
        this.ports.add(port);
    }


}
