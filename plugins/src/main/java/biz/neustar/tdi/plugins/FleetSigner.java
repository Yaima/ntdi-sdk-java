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

import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.keystructure.TdiKeyFlagsEnum;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructure;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.fw.platform.facet.crypto.TdiPlatformCryptoShape;
import biz.neustar.tdi.fw.platform.facet.data.TdiPlatformDataShape;
import biz.neustar.tdi.fw.platform.facet.keys.TdiPlatformKeysShape;
import biz.neustar.tdi.fw.platform.facet.time.TdiPlatformTimeShape;
import biz.neustar.tdi.fw.platform.facet.utils.TdiPlatformUtilsShape;
import biz.neustar.tdi.fw.plugin.TdiPluginBase;
import biz.neustar.tdi.fw.plugin.TdiPluginBaseFactory;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class FleetSigner extends TdiPluginBase {
  private static final Logger LOG = LoggerFactory.getLogger(FleetSigner.class);

  /**
   * This is populated by config and is the network location for our preferred cosigner.
   */
  private String baseURI = "";


  public FleetSigner(TdiImplementationShape impl, TdiSdkWrapperShape sdkWrapper) {
    super("FleetSigner", impl, sdkWrapper);
    LOG.trace("FleetSigner:constructor()");
  }


  @Override
  public CompletableFuture<Boolean> init() {
    LOG.trace("FleetSigner:init()");
    // Need to have configuration describing the cosigner's network location.
    return this.validatePluginDataStore(Arrays.asList("cosigner"))
      .thenCompose((Boolean confValid) -> {
        if (confValid) {
          return this.getDataStore().get("cosigner")
            .thenApply((cosignerConf) -> {
              LOG.trace("Cosigner conf loaded.");
              Map<String, Object> cosignerConfMap = (Map<String, Object>) cosignerConf;
              if (cosignerConfMap.containsKey("baseURI")) {
                this.baseURI = cosignerConfMap.get("baseURI").toString();
                LOG.info("The selected cosigner is at " + this.baseURI);
                return true;
              }
              else {
                LOG.error("FleetSigner requires a cosigner.baseURI string.");
              }
              return false;
            });
        }
        else {
          LOG.error("FleetSigner requires a cosigner configuration.");
          return CompletableFuture.supplyAsync(() -> false);
        }
      });
  }


  public void serverSign() {
    LOG.trace("FleetSigner:serverSign()");
  }

  public void serverVerify() {
    LOG.trace("FleetSigner:serverVerify()");
  }
}
