package biz.neustar.tdi.examples;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.text.StrSubstitutor;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import biz.neustar.tdi.Config;
import biz.neustar.tdi.NTDIDevice;
import biz.neustar.tdi.fw.utils.Utils;
import biz.neustar.tdi.fw.exception.InvalidFormatException;

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

    private static final String[] confList = {
      "app/dev1",
      "app/dev1"
    };


    private static Thread spinupVirtualDevice(final String confPath) {
      Thread t  = new Thread() {    // Device execution lives in this thread.
        public void run(){
          try {
            Config config = new Config(confPath);
            VirtualSensor sensor = new VirtualSensor(config.<String>get("device.name", "VirtualSensor"));
            NTDIDevice ntdi = new NTDIDevice();

            Map<String, String> map = new HashMap<String, String>();

            for (String key: Arrays.asList("gateway.id", "device.id")) {
              map.put(key, config.<String>get(key));
            }
            StrSubstitutor sub = new StrSubstitutor(map);

            // Infinite loop reporting Sensor data to data collection service
            while (true) {
              log.debug("SensorController is signing the message");
              Map<String, Object> state = sensor.getState();
              String msg = ntdi.sign(state);
              log.debug("Signed");

              log.debug("SensorController sends message to gateway");
              // TODO: Pass messgaes via brace of concurrent queues instead of MQTT.
              //this.mqtt.publish(this.dataTopic, msg.getBytes(), this.pubqos, this.retained, null, new IMqttActionListener() {
              //});
              Thread.sleep(7000);
            }
          }
          catch(IOException e) {
            log.error("Failed to read given application conf: "+confPath);
            log.debug("exception was:", e);
          }
          catch (ExecutionException e) {
            log.warn("Error processing inMsg");
            log.debug("exception was:", e);
          }
          catch (InterruptedException e) {
            log.warn("Error processing inMsg");
            log.debug("exception was:", e);
          }
          catch (InvalidFormatException e) {
            log.warn("Error processing inMsg");
            log.debug("exception was:", e);
          }
        }
      };
      t.start(); 
      return t;
    }


    public static void main(String[] args) throws Exception {
        Config config = new Config("app/config.json");
        GatewayChannel dataChannel = null;
        GatewayChannel actionChannel = null;

        for (int i = 0; i < confList.length; i++) {
          spinupVirtualDevice(confList[i]);
        }

        List<String> devices = config.<List<String>>get("sensors");

        try {
            dataChannel = new GatewayChannel(config, DATA_CLIENT_ID, LOCAL_ZONE, CLOUD_ZONE, SENSOR_DATA_TOPIC_TEMPLATE);
            dataChannel.start(devices);

            actionChannel = new GatewayChannel(config, ACTION_CLIENT_ID, CLOUD_ZONE, LOCAL_ZONE, SENSOR_ACTION_TOPIC_TEMPLATE);
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
