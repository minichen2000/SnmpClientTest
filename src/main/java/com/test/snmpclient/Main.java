package com.test.snmpclient;

import org.snmp4j.smi.OID;

import java.io.IOException;

/**
 * Created by minichen on 2016/12/12.
 */
public class Main {
    public static void main(String argv[]){
        System.out.println("Hello");
        SimpleSnmpClient client=new SimpleSnmpClient("udp:demo.snmplabs.com/161");
        try {
            System.out.println("\n//Single GET");
            System.out.println(client.getAsString(new OID("1.3.6.1.2.1.1.1.0")));

            System.out.println("\n//GETBULK-Table");
            SimpleSnmpClient.printListList(client.getTableAsStrings(
                    new OID[] {
                            //new OID("1.3.6.1.2.1.1.1"),
                            //new OID("1.3.6.1.2.1.1.5")
                            new OID("1.3.6.1.2.1.2.2.1.2"),
                            new OID("1.3.6.1.2.1.2.2.1.3")
                    })
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
