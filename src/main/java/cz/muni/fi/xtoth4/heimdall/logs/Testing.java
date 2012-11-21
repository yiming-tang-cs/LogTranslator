package cz.muni.fi.xtoth4.heimdall.logs;


import java.util.ArrayList;
import java.util.List;

public class Testing {



    public static void main(String[] args) {

        /*
           log.info("==H==" + "Jk running ID=" + wEnv.getLocalId() + " time=" + initTime + "/" + startTime +"  config=" + propFile);
           log.info("==H==" + "JK: ajp13 listening on " + getAddress() + ":" + port );
           log.info("Error shutting down the channel " + port + " " + e.toString());
        */
//        l2j = new Log2Json(String type, String level, String severity, String payload);
        Log2Json l2j = new Log2Json(Log2JsonType.NETWORK_SERVER, Log2JsonLevel.INFO, 3, "JK: ajp13 listening on \" + getAddress() + \":\" + port");


        String s1 = "log.info(\"Jk running ID=\" + wEnv.getLocalId() + \" time=\" + initTime + \"/\" + startTime +\"  config=\" + propFile);";
        // s1 type='NETWORK.SERVER', level='INFO', severity='x', payload='("Jk running ID=" + wEnv.getLocalId() + " time=" + initTime + "/" + startTime +"  config=" + propFile)';
        String s2 = "log.info(\"JK: ajp13 listening on \" + getAddress() + \":\" + port );";
        String s3 = "log.info(\"Error shutting down the channel \" + port + \" \" + e.toString());";
        List<String> strs = new ArrayList<>();
        strs.add(s1);
        strs.add(s2);
        strs.add(s3);

        for (String s : strs) {
            System.out.println(l2j);
        }
    }
}
