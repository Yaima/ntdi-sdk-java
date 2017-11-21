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

package biz.neustar.tdi.plugins;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.plugin.TdiPluginBaseFactory;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.platform.Platform;
import biz.neustar.tdi.sdk.TdiSdk;
import biz.neustar.tdi.sdk.TdiSdkOptions;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


class NTDIHelper {
  private TdiSdkWrapperShape sdkWrapper;

  public NTDIHelper(List<TdiPluginBaseFactory> plugins, String configPath) throws ExecutionException, InterruptedException {
    sdkWrapper = (new NTDIHelperFactory()).setup(plugins, configPath).get();
  }

  public String sign(String data) throws ExecutionException, InterruptedException {
    return ((TdiCanonicalMessageShape) sdkWrapper.api("SignFlow").apply(data).get()).getBuiltMessage();
  }

  public String cosign(String msg) throws ExecutionException, InterruptedException {
    return ((TdiCanonicalMessageShape) sdkWrapper.api("CosignFlow").apply(msg).get()).getBuiltMessage();
  }

  public String fleetToDevice(String data) throws ExecutionException, InterruptedException {
    return (((FleetSigner) sdkWrapper.plugin("FleetSigner")).fleetToDevice.apply(this.sign(data)).get())
        .getBuiltMessage();
  }

  public String fleetFromDevice(String msg) throws ExecutionException, InterruptedException {
    return ((FleetSigner) sdkWrapper.plugin("FleetSigner")).fleetFromDevice.apply(msg).get().getBuiltMessage();
  }

  public String verify(String msg) throws ExecutionException, InterruptedException {
    return ((String) sdkWrapper.api("VerifyFlow").apply(msg).get());
  }
}

class NTDIHelperFactory {
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

  public CompletableFuture<TdiSdkWrapperShape> setup(List<TdiPluginBaseFactory> plugins, String configPath) {
    TdiSdkOptions sdkOptions = new TdiSdkOptions();
    sdkOptions.platform = Platform::new;
    sdkOptions.plugins = plugins;
    sdkOptions.config = getConfig(configPath);
    return (new TdiSdk(sdkOptions)).init();
  }
}
