package biz.neustar.tdi;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessage;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.platform.Platform;
import biz.neustar.tdi.sdk.Constants;
import biz.neustar.tdi.sdk.TdiSdk;
import biz.neustar.tdi.sdk.TdiSdkOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Created by Yaima Valdivia on 10/5/17.
 */
public class Sensor {
    private static final Logger log = LoggerFactory.getLogger(Sensor.class);
    private static Gateway gateway;
    private static CompletableFuture<TdiSdkWrapperShape> signSdkWrapper;
    private static HashMap<String, Object> payload = new HashMap<>();

    static String[] motionSensor = {
            "active",
            "inactive"
    };

    private void initialize() {
        // Signing sdk instance
        TdiSdkOptions signSdkOptions = new TdiSdkOptions();
        signSdkOptions.platform = Platform::new;
        signSdkOptions.config = getConfig("sign/config.json");
        TdiSdk signSdk = new TdiSdk(signSdkOptions);
        signSdkWrapper = signSdk.init();
        gateway = new Gateway();
    }

    public TdiCanonicalMessage signDevice(String payload) throws Exception {
        System.out.println("\nDemo device: " + payload);
        CompletableFuture<TdiCanonicalMessage> signedMessage = signSdkWrapper
                .thenCompose((signWrapper) -> {
                    Function<String, CompletableFuture<TdiCanonicalMessage>> signApi = signWrapper
                            .api(Constants.Api.SignFlow.name());
                    CompletableFuture<TdiCanonicalMessage> signResult = signApi.apply(payload);
                    return signResult;
                });
        TdiCanonicalMessage signedMsg = signedMessage.get();
        return signedMsg;
    }

    private Map<String, Object> getConfig(String configPath) {
        Map<String, Object> map = null;
        InputStream inStream = getClass().getClassLoader().getResourceAsStream(configPath);
        try {
            map = new ObjectMapper().readValue(inStream, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static void main(String[] args) throws Exception {

        Sensor demo = new Sensor();
        demo.initialize();
        TdiCanonicalMessage signedMsg = null;
        int index;
        String motionValue;
        int batteryValue;
        Random randomNumber = new Random();

        // Infinite loop randomizing battery and motion
        while (true) {
            index = new Random().nextInt(motionSensor.length);
            motionValue = (motionSensor[index]);
            batteryValue = randomNumber.nextInt(100);
            payload.put("name", "Room 1 Sensor");
            payload.put("battery", batteryValue);
            payload.put("motion", motionValue);
            System.out.println("\nSensor is signing the message");
            signedMsg = demo.signDevice(payload.toString());
            System.out.println("\nSigned message: " + signedMsg.getBuiltMessage());
            System.out.println("\nSensor sends message to gateway");
            gateway.cosignDevice(signedMsg.getBuiltMessage());
        }
    }
}
