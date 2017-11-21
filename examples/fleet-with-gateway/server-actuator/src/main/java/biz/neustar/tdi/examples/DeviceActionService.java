/*
 * Copyright 2017 Neustar, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package biz.neustar.tdi.examples;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
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
import biz.neustar.tdi.NTDIFleet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Yaima Valdivia on 10/5/17.
 * Re-worked into a signing service by J. Ian Lindsay on 11/1/17.
 */
public class DeviceActionService {
    private static final Logger log = LoggerFactory.getLogger(DeviceActionService.class);

    public static final String DEFAULT_CONFIG_PATH = "app/config.json";

    private static final String BASE_CLIENT_ID = "action-";
    private static final String ACTION_TOPIC_TEMPLATE = "${gateway.id}/action/${device.id}";
    private static final int DEFAULT_PUBLISH_QOS = 2;
    private static final boolean DEFAULT_RETAIN_DATA_MESSAGES = false;

    private Config config;
    private NTDIFleet ntdi;
    private IMqttAsyncClient mqtt;

    public DeviceActionService() throws ExecutionException, InterruptedException, IOException {
        this(DEFAULT_CONFIG_PATH);
    }

    public DeviceActionService(String configPath) throws ExecutionException, InterruptedException, IOException {
        this(new Config(configPath));
    }

    public DeviceActionService(Config config) throws ExecutionException, InterruptedException, IOException {
        this.config = config;
        this.ntdi = new NTDIFleet();
    }

    public void start() throws IOException, MqttException {
        String broker = config.<String>get("mqtt.broker");
        final int pubqos = config.<Integer>get("mqtt.qos.publish", DEFAULT_PUBLISH_QOS);
    	MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(System.getProperty("java.io.tmpdir"));
        this.mqtt = new MqttAsyncClient(broker, BASE_CLIENT_ID + config.<String>get("server.id"), dataStore);

        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setAutomaticReconnect(true);
        opts.setCleanSession(true);

        log.debug("Connecting to broker: {}", broker);
        mqtt.connect(opts).waitForCompletion();
        log.debug("Connected");
    }

    public void action(String gatewayID, String sensorID, String action, String actionState) throws Exception {
        Map<String, String> substitutionMap = new HashMap<String, String>();

        substitutionMap.put("gateway.id", gatewayID);
        substitutionMap.put("device.id", sensorID);

        StrSubstitutor sub = new StrSubstitutor(substitutionMap);

        String topic = sub.replace(ACTION_TOPIC_TEMPLATE);

        int pubqos = config.<Integer>get("mqtt.qos.publish", DEFAULT_PUBLISH_QOS);
        boolean retained = config.<Boolean>get("mqtt.retained", DEFAULT_RETAIN_DATA_MESSAGES);

        Map<String, Object> messageMap = new HashMap<String, Object>();
        messageMap.put(action, actionState);

        log.debug("Service is signing the message");
        String message = ntdi.signForFleet(messageMap);
        log.debug("Signed and co-signed message: " + message);

        mqtt.publish(topic, message.getBytes(), pubqos, retained, null, new IMqttActionListener() {
            public void onSuccess(IMqttToken asyncActionToken) {
                log.debug("Message published to {}", topic);
            }

            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                log.warn("Error publishing data: ", exception);
            }
        });
    }

    public static void main(String[] args) throws Exception {
        Config config = new Config(DeviceActionService.DEFAULT_CONFIG_PATH);
        Map<String, List<String>> gateways = config.<Map<String, List<String>>>get("gateways");

        if (gateways == null || gateways.size() == 0) {
            log.warn("no gateways found in config: {}", config.map);
            return;
        }
        List<String> gatewayIDs = new ArrayList<String>(gateways.keySet());

        DeviceActionService svc = new DeviceActionService(config);
        Random rand = new Random();
        Map<String, String> substitionValues = new HashMap<String, String>();
        List<String> actionValues = Arrays.asList("on", "off");

        svc.start();

        while (true) {

            try {
                String gatewayID = gatewayIDs.get(rand.nextInt(gatewayIDs.size()));
                List<String> sensorIDs = gateways.get(gatewayID);
                String sensorID = sensorIDs.get(rand.nextInt(sensorIDs.size()));
                String actionState = actionValues.get(rand.nextInt(actionValues.size()));

                substitionValues.put("gateway.id", gatewayID);
                substitionValues.put("device.id", sensorID);

                log.info("turning switch *{}* for device {} on gateway {}", actionState, sensorID, gatewayID);

                svc.action(gatewayID, sensorID, "switch", actionState);

                Thread.sleep(4000);
            }
            catch (InterruptedException ie) {
                log.info("interrupted");
            }
            catch (Exception e) {
                log.warn("unexpected error, ignoring");
                log.debug("exception was:", e);
            }
        }
    }
}
