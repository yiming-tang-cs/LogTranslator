import cz.muni.fi.ngmon.logtranslator.customlogger.LogFactory;
import cz.muni.fi.ngmon.logtranslator.customlogger.Logger;
import java.net.InetAddress;

/**
 * @author mtoth
 */
public class TestingClass {

    private static final int PIA = 321321321;

    private static final Logger LOG = LogFactory.getLog(TestingClass.class);

    private int someCounter = 0;
//  ==> private static final XYZNamespace LOG = LoggerFactory.getLogger(TestingClass.class);

    private static int number;
    final int number22;

    /*  Some comment spread over
        two lines for fun.
    */
    public TestingClass(int number) {
        this.number = number;
    }

    // one liner comment
    public void add(int number) {
        final String myString = "dsa";
        int a = 2;
        // comment inside method
        LOG.info("Adding");
        this.number += number;
    }

    public void substract(int number) {
        Object client = null;
        if (LOG.isDebugEnabled()) {
            LOG.debug("MOUNT NULLOP : " + " client: " + client);
            // --> LOG.MOUNT_NULLOP_CLIENT(client).tag("cz.muni.fi.ngmon.logchanger.examplesources").debug();
        }
        LOG.log(Level.INFO, "Substracting {0}", number);
        this.number -= number;
    }

    public Object mnt(Object xdr, Object out, int xid, InetAddress client) {
        String path = xdr.toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("MOUNT MNT path: " + path + " client: " + client);
//            LOG.MOUNT_MNT_PATH_CLIENT(path, client).tag("cz.muni.fi.ngmon.logchanger.examplesources").debug();
        }

        String host = client.getHostName();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Got host: " + host + " path: " + path);
        }
        LOG.info("Path " + path + " is not shared.");
//            MountResponse.writeMNTResponse(Nfs3Status.NFS3ERR_NOENT, out, xid, null);
        return out;
    }
}
