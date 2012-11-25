package cz.muni.fi.xtoth4.heimdall.logs;

import java.io.File;
import java.util.Map;
import org.apache.log4j.Level;

public class Testing {

    private static String applicationName = "org.apache.jk.server";  // getClassName()

    public static void main(String[] args) {
        //Payload payload = new Payload();
        JsonLogger logger = new JsonLoggerImpl();
        Map<String, AttributeValue<? extends Comparable<?>>> map;// new LinkedHashMap<>(); preserve insertion-order
        /*
           log.info("==H==" + "Jk running ID=" + wEnv.getLocalId() + " time=" + initTime + "/" + startTime +"  config=" + propFile);
           log.info("==H==" + "JK: ajp13 listening on " + getAddress() + ":" + port );
           log.info("Error shutting down the channel " + port + " " + e.toString());
        */
        File file2 = new File("schemas/basic-schema.json");
        Double action = 2.34;
        String level = "TOP Level";
        Long localId = 1234567L;
        String initTime = "14:55:23.123";
        map = logger.log(applicationName + ".RUNNING", Level.INFO, file2, action, level, localId, initTime);
        writeOut(map);


        File file1 = new File("schemas/basic.json");
        action = 2.34;
        level = "TOP Level";
        localId = 1234567L;
        initTime = "14:55:23.123";
        String config = "/myConfigs/config.txt";
        Float startTime = Float.parseFloat("1234.456");
        map = logger.log(applicationName + ".RUNNING", Level.INFO, file1, action, level, localId, initTime, config, startTime);
        writeOut(map);
    }


    private static void writeOut(Map<String, AttributeValue<? extends Comparable<?>>> map) {
        for (String key : map.keySet()) {
            System.out.println(key + " : " +  map.get(key).getValue() + " (" + map.get(key).getType()+ ")");
        }
        System.out.println();
    }

}

