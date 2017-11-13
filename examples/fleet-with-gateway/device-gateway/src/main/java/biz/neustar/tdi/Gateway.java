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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Created by Yaima Valdivia on 10/5/17.
 */
public class Gateway {
    private static final Logger log = LoggerFactory.getLogger(Gateway.class);
    private static CompletableFuture<TdiSdkWrapperShape> cosignSdkWrapper;

    public void cosignDevice(String signedMsg) throws Exception {
        // Co-signing instance
        System.out.println("\nGateway receives message from sensor");
        TdiSdkOptions cosignSdkOptions = new TdiSdkOptions();
        cosignSdkOptions.platform = Platform::new;
        cosignSdkOptions.config = getConfig("cosign/config.json");
        TdiSdk cosignSdk = new TdiSdk(cosignSdkOptions);
        cosignSdkWrapper = cosignSdk.init();
        CompletableFuture<TdiCanonicalMessage> cosignedMessage = cosignSdkWrapper
                .thenCompose((cosignWrapper) -> {
                    Function<String, CompletableFuture<TdiCanonicalMessage>> cosignApi = cosignWrapper
                            .api(Constants.Api.CosignFlow.name());
                    CompletableFuture<TdiCanonicalMessage> cosignResult = cosignApi
                            .apply(signedMsg);
                    return cosignResult;
                });
        TdiCanonicalMessage cosignedMsg = cosignedMessage.get();
        System.out.println("\nGateway cosigns message");
        System.out.println("\nCosigned message: " + cosignedMsg.getBuiltMessage());
        // Sleeping thread due to clock drift between docker containers - Not advised for production
        verifySignature(cosignedMsg.getBuiltMessage());
        Thread.sleep(5000);

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

    public void verifySignature(String cosignedMsg) {

        System.out.println("\nRequest made to node-service to verify cosigned message");
        String url = "http://node-service:10010/api/v1/verifyMessage";

        try {

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // Headers
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("x-api-key", "64a8f118-d475-4c13-bcc7-2759b54175d8");

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(cosignedMsg);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            System.out.println("\nNode-service response code: " + responseCode);

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println("\nNode-service response message: " + response.toString());

        } catch (IOException exception) {
            System.out.println("\nNode-service failed to verify the message: " + exception.getMessage());
        }


    }
}
