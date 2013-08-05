package cz.muni.fi.ngmon.logchanger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import py4j.GatewayServer;

/**
 *
 * @author mtoth
 */
public class LogChangerEntryPoint {
    
    private LogChanger logChanger;

    public LogChangerEntryPoint() {
      logChanger = new LogChanger();      
//      logChanger.setNewLog("asda");
//      logChanger.setOldLog("asd");
//      logChanger.setVariables(Arrays.asList("jedna", "dva"));
    }

    public LogChanger getLogChanger() {
        return logChanger;
    }
    
    public TreeWalker getTreeWalker() {
        return new TreeWalker();
    }
    
    /**
     If default port [25333] is occupied, use following
    GatewayServer gatewayServer = new GatewayServer(new StackEntryPoint(), 25335);
    In python use
    from py4j.java_gateway import JavaGateway, GatewayClient
    gateway = JavaGateway(GatewayClient(port=25335))    
    */
    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new LogChangerEntryPoint());

        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }

}
