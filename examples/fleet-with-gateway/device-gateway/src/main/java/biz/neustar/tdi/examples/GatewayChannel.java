package biz.neustar.tdi.examples;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.text.StrSubstitutor;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
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
public class GatewayChannel implements IMqttMessageListener {
    private static final Logger log = LoggerFactory.getLogger(GatewayChannel.class);

    private static final int DEFAULT_PUBLISH_QOS = 2;
    private static final int DEFAULT_SUBSCRIBE_QOS = 1;
    private static final boolean DEFAULT_RETAIN_DATA_MESSAGES = false;

    private Config config;
    private String clientID;
    private String inZone;
    private String outZone;
    private String topicTemplate;

    private int pubqos;
    private int subqos;
    private boolean retained;

    private NTDIGateway ntdi;

    private IMqttAsyncClient inBroker;
    private IMqttAsyncClient outBroker;

    public GatewayChannel(Config config, String clientID, String inZone, String outZone, String topicTemplate)
            throws ExecutionException, InterruptedException, IOException {
        this.config = config;
        this.clientID = clientID;
        this.inZone = inZone;
        this.outZone = outZone;
        this.topicTemplate = topicTemplate;

        this.subqos = config.<Integer>get(String.format("mqtt.%s.qos.subscribe", inZone), DEFAULT_SUBSCRIBE_QOS);

        this.pubqos = config.<Integer>get(String.format("mqtt.%s.qos.publish", outZone), DEFAULT_PUBLISH_QOS);
        this.retained = config.<Boolean>get(String.format("mqtt.%s.retained", outZone), DEFAULT_RETAIN_DATA_MESSAGES);

        this.ntdi = new NTDIGateway();
    }

    public void start(List<String> devices) {
        try {
            this.stop();  // just in case

            this.inBroker = this.connect(this.inZone);
            this.outBroker = this.connect(this.outZone);


            for (String device: devices) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("gateway.id", config.<String>get("device.id"));
                map.put("device.id", device);
                StrSubstitutor sub = new StrSubstitutor(map);

                String topic = sub.replace(this.topicTemplate);
                log.debug("listening on topic {}", topic);

                this.inBroker.subscribe(topic, this.subqos, null, new IMqttActionListener() {
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
        if (this.inBroker != null) this.disconnect(this.inBroker, this.inZone);
        if (this.outBroker != null) this.disconnect(this.outBroker, this.outZone);
    }

    // from IMqttMessageListener

    public void messageArrived(java.lang.String topic, MqttMessage inMsg) {
        log.info("incoming msg on {}", topic);
        log.debug("{}", inMsg);

        try {
            if (this.ntdi.verifyFromDevice(inMsg.toString()) == null) {
                // shouldn't get here, exception should be thrown
                log.warn("message from device failed to validate");
            }
            else {
                String outMsg = this.ntdi.cosign(inMsg.toString());
                log.debug("Cosigned message: " + outMsg);

                this.outBroker.publish(topic, outMsg.getBytes(), this.pubqos, this.retained, null, new IMqttActionListener() {
                    public void onSuccess(IMqttToken asyncActionToken) {
                        log.info("Message published to {}", topic);
                        log.debug("{}", outMsg);
                    }

                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        log.warn("Error publishing data: ", exception);
                    }
                });
            }
        }
        catch (MqttException me) {
            log.debug("Error forwarding inMsg", me);
        }
        catch (Exception e) {
            log.warn("Error processing inMsg", e);
        }
    }

    // private methods

    private IMqttAsyncClient connect(String zone) throws MqttException {
        String broker = this.config.<String>get(String.format("mqtt.%s.broker", zone));
    	MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(System.getProperty("java.io.tmpdir"));

        IMqttAsyncClient ret = new MqttAsyncClient(broker, this.clientID + "-" + this.config.<String>get("device.id"), dataStore);

        log.debug("Connecting to {} MQTT Broker: {}", zone, broker);
        ret.connect().waitForCompletion();
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
