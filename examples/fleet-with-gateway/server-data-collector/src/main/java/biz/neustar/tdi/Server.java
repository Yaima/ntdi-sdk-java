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

package biz.neustar.tdi;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessage;
import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.platform.Platform;
import biz.neustar.tdi.plugins.FleetSigner;
import biz.neustar.tdi.sdk.Constants;
import biz.neustar.tdi.sdk.TdiSdk;
import biz.neustar.tdi.sdk.TdiSdkOptions;
import biz.neustar.tdi.sdk.Constants.Api;
import biz.neustar.tdi.fw.plugin.TdiPluginBaseFactory;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.ArrayList;

/**
 * Created by Yaima Valdivia on 10/5/17.
 * Re-worked into a signing service by J. Ian Lindsay on 11/1/17.
 */
public class Server {
  private static final Logger LOG = LoggerFactory.getLogger(Server.class);
  private static HashMap<String, Object> payload = new HashMap<>();

  public TdiSdkWrapperShape Sdk = null;


  private Map<String, Object> getConfig(String configPath) {
    Map<String, Object> map = null;
    InputStream inStream = getClass().getClassLoader().getResourceAsStream(configPath);
    try {
      map = new ObjectMapper().readValue(inStream, new TypeReference<Map<String, Object>>() {});
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return map;
  }

  public void dumpAPIToConsole() {
    System.out.println();
  }

  public int initialize() {
    CompletableFuture<TdiSdkWrapperShape> signSdkWrapper;
    TdiSdkOptions signSdkOptions = new TdiSdkOptions();
    signSdkOptions.platform = Platform::new;
    signSdkOptions.config = getConfig("sign/config.json");
    signSdkOptions.plugins = new ArrayList<TdiPluginBaseFactory>();
    signSdkOptions.plugins.add(FleetSigner::new);
    TdiSdk signSdk = new TdiSdk(signSdkOptions);
    signSdkWrapper = signSdk.init()
      .thenApply((TdiSdkWrapperShape sdk) -> {
        this.Sdk = sdk;
        LOG.trace("\nServer initialized.");
        TdiCanonicalMessage signedMsg = null;
        return sdk;
      });
    try {
      signSdkWrapper.get(5, TimeUnit.SECONDS);
      return 0;
    }
    catch (Exception e) {
      //CancellationException
      //ExecutionException
      //InterruptedException
      //TimeoutException
      String errMsg = "SDK init failed.";
      return -1;
    }
  }


  public CompletableFuture<TdiCanonicalMessageShape> sendDeviceMessage(String msg_text) {
    Function<String, CompletableFuture<TdiCanonicalMessageShape>> signApi = this.Sdk.api(Api.SignFlow.name());
    LOG.info("Calling FleetSigner method fleetFromDevice.");
    CompletableFuture<TdiCanonicalMessageShape> signResult = signApi.apply(msg_text);
    return signResult;
  }

  public CompletableFuture<TdiCanonicalMessageShape> sendFSMessage0(String msg_text) {
    FleetSigner fs = (FleetSigner) this.Sdk.plugin("FleetSigner");
    LOG.info("Calling FleetSigner method fleetToDevice.");
    Function<String, CompletableFuture<TdiCanonicalMessageShape>> signApi = fs.fleetToDevice;
    CompletableFuture<TdiCanonicalMessageShape> signResult = signApi.apply(msg_text);
    return signResult;
  }

  public CompletableFuture<TdiCanonicalMessageShape> sendFSMessage1(String msg_text) {
    FleetSigner fs = (FleetSigner) this.Sdk.plugin("FleetSigner");
    Function<String, CompletableFuture<TdiCanonicalMessageShape>> signApi = fs.fleetFromDevice;
    CompletableFuture<TdiCanonicalMessageShape> signResult = signApi.apply(msg_text);
    return signResult;
  }


  public static void main(String[] args) throws Exception {
    Server demo = new Server();
    if (0 == demo.initialize()) {
      Thread.sleep(1000);
      //CompletableFuture<TdiCanonicalMessageShape> msg0 = demo.sendDeviceMessage("This is bound for a device")
      //  .thenApply((TdiCanonicalMessageShape msgObj) -> {
      //    LOG.info("Data over the wire for normal signing flow: \n"+msgObj.getRawPayload());
      //    return msgObj;
      //  });
      CompletableFuture<TdiCanonicalMessageShape> msg1 = demo.sendFSMessage0("This is signed by the fleet, and directed toward a device.")
        .thenApply((TdiCanonicalMessageShape msgObj) -> {
          LOG.info("Data over the wire for FS signing flow: \n"+msgObj.getRawPayload());
          return msgObj;
        });
      //CompletableFuture<TdiCanonicalMessageShape> msg2 = demo.sendFSMessage1("This is signed by a device, and directed toward the fleet.")
      //  .thenApply((TdiCanonicalMessageShape msgObj) -> {
      //    LOG.info("Data over the wire for FS signing flow: \n"+msgObj.getRawPayload());
      //    return msgObj;
      //  });
      while(true);
    }
  }
}
