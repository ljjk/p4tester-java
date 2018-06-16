package org.p4tester;


import org.p4tester.packet.Ethernet;
import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.MacAddress;
import org.pcap4j.util.NifSelector;

import java.io.IOException;
import java.util.ArrayList;

public class P4TesterProbeProcessor implements PacketListener {
    private PcapNetworkInterface nif;
    private String devName;
    private PcapHandle handle;
    private ArrayList<NetworkProbeSet> networkProbeSets;
    static final int PACKET_COUNT = 1000;


    P4TesterProbeProcessor(ArrayList<NetworkProbeSet> networkProbeSets) {
        /*
        try {
            this.nif = new NifSelector().selectNetworkInterface();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        //if (nif == null) {
        //    return;
        //}
        //System.out.println("Open the device: " + this.nif.getName());
        /*
        try {
            this.handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);

        } catch (PcapNativeException e) {
            e.printStackTrace();
        }
        */
        this.networkProbeSets = networkProbeSets;
    }

    public void loop() {
        try {
            this.handle.loop(PACKET_COUNT, this);
        } catch (PcapNativeException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NotOpenException e) {
            e.printStackTrace();
        }
    }

    public void setNetworkProbeSets(ArrayList<NetworkProbeSet> networkProbeSets) {
        this.networkProbeSets = networkProbeSets;
    }

    public void injectProbes() {
        System.out.println("1");
        if (networkProbeSets != null) {
            for (NetworkProbeSet networkProbeSet:networkProbeSets) {
                for (Ethernet ethernet:networkProbeSet.generateProbes()) {
                    System.out.println("1");
                    // try {
                    //     this.handle.sendPacket(ethernet.serialize());
                    //} catch (NotOpenException e) {
                    //    e.printStackTrace();
                    //} catch (PcapNativeException e) {
                    //    e.printStackTrace();
                    //}
                }
            }
        }
    }

    @Override
    public void gotPacket(Packet packet) {
        Ethernet ethernet = new Ethernet();
        ethernet.deserialize(packet.getRawData(), 0, packet.length());
    }

}