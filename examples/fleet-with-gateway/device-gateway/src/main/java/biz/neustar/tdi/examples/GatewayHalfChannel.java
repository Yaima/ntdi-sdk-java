package biz.neustar.tdi.examples;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

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
import biz.neustar.tdi.NTDIGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Yaima Valdivia on 10/5/17.
 */
public class GatewayHalfChannel implements IMqttMessageListener {
    private static final Logger log = LoggerFactory.getLogger(GatewayChannel.class);

    private static final int DEFAULT_PUBLISH_QOS = 2;
    private static final int DEFAULT_SUBSCRIBE_QOS = 1;
    private static final boolean DEFAULT_RETAIN_DATA_MESSAGES = false;

    private Config config;
    private String clientID;
    private String zone;
    private String topicTemplate;

    private int pubqos;
    private int subqos;
    private boolean retained;

    public Function<byte[], Boolean> netAcceptFxn;

    private NTDIGateway ntdi;

    private IMqttAsyncClient broker;

    public GatewayHalfChannel(Config config, String clientID, String z, String topicTemplate, Function<byte[], Boolean> na_fxn)
        throws ExecutionException, InterruptedException, IOException {
      this.config = config;
      this.clientID = clientID;
      this.zone = z;
      this.topicTemplate = topicTemplate;

      this.netAcceptFxn = na_fxn;

      this.subqos = config.<Integer>get(String.format("mqtt.%s.qos.subscribe", this.zone), DEFAULT_SUBSCRIBE_QOS);
      this.pubqos = config.<Integer>get(String.format("mqtt.%s.qos.publish", this.zone), DEFAULT_PUBLISH_QOS);
      this.retained = config.<Boolean>get(String.format("mqtt.%s.retained", this.zone), DEFAULT_RETAIN_DATA_MESSAGES);

      this.ntdi = new NTDIGateway();
    }

    public void start(List<String> devices) {
      try {
        this.stop();  // just in case
        this.broker = this.connect(this.zone);

        for (String device: devices) {
          Map<String, String> map = new HashMap<String, String>();
          map.put("gateway.id", config.<String>get("device.id"));
          map.put("device.id", device);
          StrSubstitutor sub = new StrSubstitutor(map);
          String topic = sub.replace(this.topicTemplate);
          log.debug("listening on topic {}", topic);
          this.broker.subscribe(topic, this.subqos, null, new IMqttActionListener() {
            // TODO: Leverage this asynchronicity later...
            public void onSuccess(IMqttToken asyncActionToken) {
              log.info("Subscribed to {}", topic);
            }
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
              log.warn("Error subscribing: ", exception);
            }
          }, this);
        }
      }
      catch(MqttException me) {
        log.warn("MQTT error", me);
        this.stop();
      }
    }

    public void stop() {
      if (null != this.broker) {
        this.disconnect(this.broker, this.zone);
      }
    }

    // from IMqttMessageListener

    public void messageArrived(java.lang.String topic, MqttMessage inMsg) {
      log.info("IN  <- {}", topic);
      log.debug("incoming message: {}", inMsg);

      try {
        // TODO: This might not be a good idea.... test...
        netAcceptFxn.apply(inMsg.toString().getBytes());
      }
      catch (Exception e) {
        log.warn("Error processing incoming message", e);
        log.debug("exception was:", e);
      }
    }

  public boolean cosignAndPublish(String inbound) {
    // TODO: This conditional rests on the blocking resolution of a very
    //   complex asynchronous function. This would be a good point to make
    //   the comm channel async.
    try {
      if (null != this.ntdi.verifyFromDevice(inbound)) {
        // Call a constructor-provided async fxn that deposits into the message queue.
        String pubPayload = this.ntdi.cosign(inbound);
        log.debug("Cosigned message: " + pubPayload);
        // TODO: kid is related to topic, but I'm not keen to decompile this string and dig out a kid value.
        //   Need Canonical message, or a layer-3 inferance passed in from the caller. We presently
        //   have neither.
        String topic = "Some fake kid";
        this.broker.publish(topic, pubPayload.getBytes(), this.pubqos, this.retained, null, new IMqttActionListener() {
            public void onSuccess(IMqttToken asyncActionToken) {
              log.info("OUT -> {}", topic);
              log.debug("outgoing message: {}", pubPayload);
            }
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
              log.warn("Error publishing incoming message");
              log.debug("exception was: ", exception);
            }
          });
        return true;
      }
    }
    catch (ExecutionException e) {
      log.warn("Error processing device efferent message.");
      log.debug("exception was:", e);
    }
    catch (InterruptedException e) {
      log.warn("Error processing device efferent message.");
      log.debug("exception was:", e);
    }
    catch (MqttException e) {
      log.error("MQTT error while publishing GW-signed message: ", e);
      this.stop();
    }
    // shouldn't get here, exception should be thrown
    log.warn("message from device failed to validate");
    return false;
  }


  // private methods

  private IMqttAsyncClient connect(String zone) throws MqttException {
    String broker = this.config.<String>get(String.format("mqtt.%s.broker", zone));
    MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(System.getProperty("java.io.tmpdir"));

    IMqttAsyncClient ret = new MqttAsyncClient(broker, this.clientID + "-" + this.config.<String>get("device.id"), dataStore);

    MqttConnectOptions opts = new MqttConnectOptions();
    opts.setAutomaticReconnect(true);
    opts.setCleanSession(true);

    log.debug("Connecting to {} MQTT Broker: {}", zone, broker);
    ret.connect(opts).waitForCompletion();
    log.debug("Connected {} client to {} MQTT Broker", this.clientID, zone);

    return ret;
  }


  private void disconnect(IMqttAsyncClient broker, String zone) {
    try {
      broker.disconnect().waitForCompletion();
      log.debug("Disconnected {} client from {} MQTT Broker", this.clientID, zone);
    }
    catch (MqttException me) {
      log.warn("Error disconnecting from broker", me);
    }
  }
}
