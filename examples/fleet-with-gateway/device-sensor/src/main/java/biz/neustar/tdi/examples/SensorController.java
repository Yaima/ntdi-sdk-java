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
public class SensorController implements IMqttMessageListener {
    private static final Logger log = LoggerFactory.getLogger(SensorController.class);

    private static final String DATA_TOPIC_TEMPLATE = "${gateway.id}/report/${device.id}";
    private static final String ACTION_TOPIC_TEMPLATE = "${gateway.id}/action/${device.id}";

    private static final int DEFAULT_SUBSCRIBE_QOS = 1;
    private static final int DEFAULT_PUBLISH_QOS = 2;
    private static final boolean DEFAULT_RETAIN_DATA_MESSAGES = false;
    private static final int DEFAULT_REPORTING_INTERVAL = 1000;

    private static int counter = 1;
    private static final String DEFAULT_SENSOR_NAME = String.format("Unnamed Sensor #%d", SensorController.counter++);

    private Config config;
    private Sensor sensor;
    private NTDIDevice ntdi;

    private String dataTopic;
    private String actionTopic;
    int pubqos;
    int subqos;
    boolean retained;
    int reportInterval;

    IMqttAsyncClient mqtt;


    public SensorController(Config config, Sensor sensor) throws ExecutionException, InterruptedException, IOException {
        this.sensor = sensor;
        this.config = config;
        this.ntdi = new NTDIDevice(new Config("tdi/config.json", true));

        Map<String, String> map = new HashMap<String, String>();

        for (String key: Arrays.asList("gateway.id", "device.id")) {
            map.put(key, this.config.<String>get(key));
        }
        StrSubstitutor sub = new StrSubstitutor(map);

        this.dataTopic = sub.replace(DATA_TOPIC_TEMPLATE);
        this.actionTopic = sub.replace(ACTION_TOPIC_TEMPLATE);

        log.debug("dataTopic is {}, actionTopic is {}", dataTopic, actionTopic);

        this.pubqos = this.config.<Integer>get("mqtt.qos.publish", DEFAULT_PUBLISH_QOS);
        this.subqos = this.config.<Integer>get("mqtt.qos.subscribe", DEFAULT_SUBSCRIBE_QOS);
        this.retained = this.config.<Boolean>get("mqtt.retained", DEFAULT_RETAIN_DATA_MESSAGES);
        this.reportInterval = this.config.<Integer>get("reporting.interval", DEFAULT_REPORTING_INTERVAL);
    }

    public void messageArrived(java.lang.String topic, MqttMessage message) {
        log.debug("msg received on {}: {}", topic, message);

        try {
            String payload = this.ntdi.verify(message.toString());

            if (payload == null) {
                // shouldn't get here, exception should be thrown
                log.warn("message from device failed to validate");
            }
            else {
                log.info("RECEIVED: {}", payload);
                Map<String, Object> data = Utils.jsonToMap(payload);

                for (String action: data.keySet()) {
                    switch (action) {
                      case "switch":
                        sensor.setSwitchState(Sensor.SwitchState.valueOf(data.get(action).toString()));
                        this.sendSensorState(true);
                        break;

                      default:
                        log.debug("invalid action: '{}', ignoring", action);
                        break;
                    }
                }
            }
        }
        catch (Exception e) {
            log.warn("Error processing inMsg");
            log.debug("exception was:", e);
        }
    }

    public void run() throws InterruptedException, InvalidFormatException, MqttException {
        String broker = this.config.<String>get("mqtt.broker");
	    MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(System.getProperty("java.io.tmpdir"));
        this.mqtt = new MqttAsyncClient(broker, this.config.<String>get("device.id"), dataStore);

        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setAutomaticReconnect(true);
        opts.setCleanSession(true);

        log.debug("Connecting to broker: {}", broker);
        this.mqtt.connect(opts).waitForCompletion();
        log.debug("Connected");

        this.mqtt.subscribe(this.actionTopic, this.subqos, this);

        // Infinite loop reporting Sensor data to data collection service
        while (true) {
            try {
                this.sendSensorState(false);
            }
            catch (ExecutionException | MqttException e) {
                log.warn("error sending sensor state");
                log.debug("exception was:", e);
            }
            Thread.sleep(reportInterval);
        }
    }

    private void sendSensorState(boolean wasChanged) throws ExecutionException, InterruptedException, InvalidFormatException, MqttException {
        log.debug("SensorController is signing the message");
        Map<String, Object> state = sensor.getState();
        String msg = this.ntdi.sign(state);
        log.debug("Signed");

        log.debug("SensorController sends message to gateway");
        this.mqtt.publish(this.dataTopic, msg.getBytes(), this.pubqos, this.retained, null, new IMqttActionListener() {
            public void onSuccess(IMqttToken asyncActionToken) {
                log.debug("Message published to {}", dataTopic);
                log.info("SENT: {}", state);
            }

            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                log.warn("Error publishing data");
                log.debug("exception was: ", exception);
            }
        });
    }

    public static void main(String[] args) throws Exception {
        Config config = new Config("app/config.json", true);
        Sensor sensor = new Sensor(config.<String>get("device.name", DEFAULT_SENSOR_NAME));
        SensorController sensorController = new SensorController(config, sensor);

        try {
            sensorController.run();
        }
        catch(InterruptedException ie) {
            log.debug("Disconnected");
        }
    }
}
