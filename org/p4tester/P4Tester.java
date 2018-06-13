package org.p4tester;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class P4Tester {
    private ArrayList<Router> routers;
    private P4TesterBDD bdd;

    P4Tester(P4TesterBDD bdd) {
        this.bdd = bdd;
        routers = new ArrayList<>();
    }

    public void encodeInternet2(String router_name, String fileName) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(fileName));
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line = null;

            for (int i =0 ;i < 7; i++) {
                reader.readLine();
            }
            ArrayList<String> entry = new ArrayList<>();
            while((line = reader.readLine()) != null) {
                String[] info = line.split(" ");
                for (String i:info) {
                    if (i.length() > 0) {
                        System.out.println(i);
                    }
                }

                entry.clear();
                break;
            }

            inputStreamReader.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void encodeStanford(String router_name, String fileName) {

    }

    public void buildBDDTree() {

    }


    public void internal_test() {
        this.encodeInternet2("", "/Users/zhouyu/p4tester/resource/data/Internet2/atla-show_route_forwarding-table_table_default.xml");
    }


}
