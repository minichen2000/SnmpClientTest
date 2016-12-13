package com.test.snmpclient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;


/**
 * Simplest client possible
 *
 * @author johanrask
 *
 */
public class SimpleSnmpClient {

    private String address;

    private Snmp snmp;


    public SimpleSnmpClient(String address) {
        super();
        this.address = address;
        try {
            start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Since snmp4j relies on asynch req/resp we need a listener
    // for responses which should be closed
    public void stop() throws IOException {
        snmp.close();
    }

    private void start() throws IOException {
        TransportMapping transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        // Do not forget this line!
        transport.listen();
    }

    public String getAsString(OID oid) throws IOException {
        ResponseEvent event = get(new OID[]{oid});
        PDU pdu=event.getResponse();
        if(null!=pdu){
            System.out.println(pdu.toString());
            return pdu.get(0).getVariable().toString();
        }else{
            return null;
        }
    }


    public void getAsString(OID oids,ResponseListener listener) {
        try {
            snmp.send(getPDU(new OID[]{oids}), getTarget(),null, listener);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private PDU getPDU(OID oids[]) {
        PDU pdu = new PDU();
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }

        pdu.setType(PDU.GET);
        return pdu;
    }

    public ResponseEvent get(OID oids[]) throws IOException {
        ResponseEvent event = snmp.send(getPDU(oids), getTarget(), null);
        if(event != null) {
            return event;
        }
        throw new RuntimeException("GET timed out");
    }

    private Target getTarget() {
        Address targetAddress = GenericAddress.parse(address);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    /**
     * Normally this would return domain objects or something else than this...
     */
    public List<List<String>> getTableAsStrings(OID[] oids) {
        TableUtils tUtils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GET));

        @SuppressWarnings("unchecked")
        List<TableEvent> events = tUtils.getTable(getTarget(), oids, null, null);
        List<List<String>> list = new ArrayList<List<String>>();
        for (TableEvent event : events) {
            if(event.isError()) {
                throw new RuntimeException(event.getErrorMessage());
            }
            List<String> strList = new ArrayList<String>();
            list.add(strList);
            for(VariableBinding vb: event.getColumns()) {
                strList.add(vb.getVariable().toString());
            }
        }
        return list;
    }
    public List<List<VariableBinding>> getTable(OID[] oids) {
        TableUtils tUtils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));

        @SuppressWarnings("unchecked")
        List<TableEvent> events = tUtils.getTable(getTarget(), oids, null, null);

        List<List<VariableBinding>> list = new ArrayList<List<VariableBinding>>();
        for (TableEvent event : events) {
            if(event.isError()) {
                throw new RuntimeException(event.getErrorMessage());
            }
            List<VariableBinding> row = new ArrayList<VariableBinding>();
            for(VariableBinding vb: event.getColumns()) {
                row.add(vb);
            }
            list.add(row);
        }
        return list;
    }

    public static String extractSingleString(ResponseEvent event) {
        return event.getResponse().get(0).getVariable().toString();
    }

    public static void printList(List<VariableBinding> l){
        for(VariableBinding vb : l){
            String att="";
            if(null!=vb) att=vb.toString();
            System.out.println(att);
        }
    }
    public static void printListList(List<List<VariableBinding>> l){
        for(List<VariableBinding> s : l){
            printList(s);
            System.out.println("-------------------------------------------------");
        }
    }
}
