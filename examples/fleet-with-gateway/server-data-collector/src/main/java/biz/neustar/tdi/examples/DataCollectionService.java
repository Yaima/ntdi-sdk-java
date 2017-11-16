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
import biz.neustar.tdi.NTDIFleet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Yaima Valdivia on 10/5/17.
 * Re-worked into a signing service by J. Ian Lindsay on 11/1/17.
 */
public class DataCollectionService {
    private static final Logger log = LoggerFactory.getLogger(DataCollectionService.class);

    public static final String DEFAULT_CONFIG_PATH = "app/config.json";

    private static final String BASE_CLIENT_ID = "data-";
    private static final String SENSOR_DATA_TOPIC_TEMPLATE = "${gateway.id}/report/${device.id}";
    private static final int DEFAULT_SUBSCRIBE_QOS = 1;

    private Config config;
    private NTDIFleet ntdi;

    public DataCollectionService() throws ExecutionException, InterruptedException, IOException {
        this(DEFAULT_CONFIG_PATH);
    }

    public DataCollectionService(String configPath) throws ExecutionException, InterruptedException, IOException {
        this(new Config(configPath));
    }

    public DataCollectionService(Config config) throws ExecutionException, InterruptedException, IOException {
        this.config = config;
        this.ntdi = new NTDIFleet();
    }

    public int run() throws IOException, MqttException {
        Config config = new Config("app/config.json");
        IMqttAsyncClient mqtt = null;

        String broker = config.<String>get("mqtt.broker");
        final int subqos = config.<Integer>get("mqtt.qos.subscribe", DEFAULT_SUBSCRIBE_QOS);
    	MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(System.getProperty("java.io.tmpdir"));
        mqtt = new MqttAsyncClient(broker, BASE_CLIENT_ID + config.<String>get("server.id"), dataStore);

        log.debug("Connecting to broker: {}", broker);
        mqtt.connect().waitForCompletion();
        log.debug("Connected");

        Map<String, List<String>> gateways = config.<Map<String, List<String>>>get("gateways");

        if (gateways == null || gateways.size() == 0) {
            log.debug("no gateways found in config: {}", config.map);
            return 1;
        }
        Map<String, String> substitionValues = new HashMap<String, String>();

        IMqttMessageListener msgListener = new IMqttMessageListener() {
            public void messageArrived(java.lang.String topic, MqttMessage message) {
                log.info("msg received on {}: {}", topic, message);
                // this.ntdi.verify(message.toString());
            }
        };

        for (Map.Entry<String, List<String>> entry: gateways.entrySet()) {
            String gatewayID = entry.getKey();
            List<String> sensors = entry.getValue();

            substitionValues.put("gateway.id", gatewayID);

            for (String sensorID: sensors) {
                substitionValues.put("device.id", sensorID);
                StrSubstitutor sub = new StrSubstitutor(substitionValues);

                String topic = sub.replace(SENSOR_DATA_TOPIC_TEMPLATE);

                mqtt.subscribe(topic, subqos, null, new IMqttActionListener() {
                    public void onSuccess(IMqttToken asyncActionToken) {
                        log.info("Subscribed to {}", topic);
                    }

                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        log.warn("Error subscribing: ", exception);
                    }
                }, msgListener);
            }
        }

        try {
            // TODO: something better
            while(true) Thread.sleep(4000);
        }
        catch(InterruptedException ie) {
            log.debug("interrupted", ie);

            if (mqtt != null) {
                mqtt.disconnect().waitForCompletion();
                log.info("Disconnected");
            }
        }
        return 0;
    }

    public static void main(String[] args) throws Exception {
        DataCollectionService svc = new DataCollectionService();
        System.exit(svc.run());
    }
}
