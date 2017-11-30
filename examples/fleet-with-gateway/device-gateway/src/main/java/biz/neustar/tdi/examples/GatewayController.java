package biz.neustar.tdi.examples;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

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

  private static final String ACTION_CLIENT_ID = "action";

  private static final String CLOUD_ZONE = "cloud";

  private static final String SENSOR_ACTION_TOPIC_TEMPLATE = "${gateway.id}/action/${device.id}";

  private static final int GW_MAX_MSG_BACKLOG = 20;
  private static final int DEVICE_REPORT_PERIOD_MS = 7000;
  private static final String[] confList = {
    "app/dev1",
    "app/dev2",
    "app/dev3",
    "app/dev4",
    "app/dev5",
    "app/dev6"
  };

  // With-respect-to gateway program, this is the inbound listening pipe. All
  //   messages from all devices arrive here.
  // The gateway can only accept GW_MAX_MSG_BACKLOG messages before .put() stalls
  //   and .offer() fails.
  // If Virtual devices fill this queue fast than the gateway can relay, you
  //   will experience "transport failures".
  public static LinkedBlockingQueue<byte[]> gw_inbound = new LinkedBlockingQueue<byte[]>(GW_MAX_MSG_BACKLOG);

  // Unicast outbound  with-respect-to gateway program. Each device contributes
  //   an entry to this list. Please mind the awful generics. Must be thread-safe.
  // This only has as many slots as we have confLists.
  public static LinkedBlockingQueue<LinkedBlockingQueue<byte[]>> gw_outbound = new LinkedBlockingQueue<LinkedBlockingQueue<byte[]>>(confList.length);


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

          // This is the pipe we give back to the Gateway for the sake of receiving
          //   messages from it. This pipe is unicast, so anything that comes back
          //   from it is ipso facto meant for us.
          LinkedBlockingQueue<byte[]> dev_inbound = new LinkedBlockingQueue<byte[]>();
          if (!gw_outbound.offer(dev_inbound)) {
            // The network connection was rejected.
            return;
          }


          // Infinite loop reporting Sensor data to data collection service
          while (true) {
            log.debug("SensorController is signing the message");
            Map<String, Object> state = sensor.getState();
            String msg = ntdi.sign(state);
            log.debug("Signed");

            log.debug("SensorController sends message to gateway");
            // Pass messgaes via brace of concurrent queues. Whatever transport
            //   arrangements might be made for a given brownfield device is assumed
            //   to be handled, but we still need a means of relay out of the thread.
            // The call below will block until queue accepts. It is therefore
            //   analogous to an ordered reliable transport like TCP, despite not
            //   being a stream.
            gw_inbound.put(msg.getBytes());
            // If you want to model an unreliable transport like UDP, use this line instead.
            //gw_inbound.offer(msg.getBytes());
            Thread.sleep(DEVICE_REPORT_PERIOD_MS);
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
    GatewayHalfChannel actionChannel = null;

    for (int i = 0; i < confList.length; i++) {
      spinupVirtualDevice(confList[i]);
    }

    List<String> devices = config.<List<String>>get("sensors");
    try {
      actionChannel = new GatewayHalfChannel(config, ACTION_CLIENT_ID, CLOUD_ZONE, SENSOR_ACTION_TOPIC_TEMPLATE,
        (m) -> {
          // TODO: This is where logic should go to figure out the identity of the
          //   virtual device this message is intended for, and the .put() to
          //   the appropriate queue.
          return false;
        }
      );
      actionChannel.start(devices);

      // TODO: something better
      while(true) {
        byte[] current_inbound = gw_inbound.take();
        if (null != current_inbound) {
          log.debug("Signing the message from VirtDev");
          actionChannel.cosignAndPublish(current_inbound.toString());
        }
      }
    }
    catch(InterruptedException ie) {
      log.debug("interrupted", ie);

      if (null != actionChannel) {
        actionChannel.stop();
      }
    }
  }
}
