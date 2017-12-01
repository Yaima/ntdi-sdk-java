package biz.neustar.tdi.examples;

import java.util.List;

import biz.neustar.tdi.Config;
import biz.neustar.tdi.examples.GatewayChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Yaima Valdivia on 10/5/17.
 */
public class GatewayController {
    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);

    private static final String DATA_CLIENT_ID = "data";
    private static final String ACTION_CLIENT_ID = "action";

    private static final String LOCAL_ZONE = "local";
    private static final String CLOUD_ZONE = "cloud";

    private static final String SENSOR_DATA_TOPIC_TEMPLATE = "${gateway.id}/report/${device.id}";
    private static final String SENSOR_ACTION_TOPIC_TEMPLATE = "${gateway.id}/action/${device.id}";

    public static void main(String[] args) throws Exception {
        Config config = new Config("app/config.json", true);
        GatewayChannel dataChannel = null;
        GatewayChannel actionChannel = null;

        List<String> devices = config.<List<String>>get("sensors");

        try {
            dataChannel = new GatewayChannel(config, DATA_CLIENT_ID, LOCAL_ZONE, CLOUD_ZONE, SENSOR_DATA_TOPIC_TEMPLATE, false);
            dataChannel.start(devices);

            actionChannel = new GatewayChannel(config, ACTION_CLIENT_ID, CLOUD_ZONE, LOCAL_ZONE, SENSOR_ACTION_TOPIC_TEMPLATE, true);
            actionChannel.start(devices);

            // TODO: something better
            while(true) Thread.sleep(4000);
        }
        catch(InterruptedException ie) {
            log.debug("interrupted", ie);

            if (dataChannel != null) {
                dataChannel.stop();
            }

            if (actionChannel != null) {
                actionChannel.stop();
            }
        }
    }
}
