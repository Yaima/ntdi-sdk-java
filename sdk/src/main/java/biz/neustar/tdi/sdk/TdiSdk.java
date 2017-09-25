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

package biz.neustar.tdi.sdk;

import biz.neustar.tdi.fw.classfactory.TdiClassFactory;
import biz.neustar.tdi.fw.classfactory.TdiClassFactoryShape;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import biz.neustar.tdi.fw.implementation.TdiFlowArguments;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapper;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import biz.neustar.tdi.sdk.api.TdiSdkApiFactory;
import biz.neustar.tdi.sdk.component.ComponentUtils;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

/**
 * Core class to initiate the sdk.
 *
 */
public class TdiSdk {

  /**
   * Options of the {@link TdiSdk}.
   */
  TdiSdkOptions sdkOptions;

  /**
   * Constructor.
   * 
   * @param sdkOptions
   *          : {@link TdiSdkOptions} object.
   * 
   * @throws FrameworkRuntimeException
   *           if 1. arguments are not valid 2. there is an issue with loading
   *           plugins
   */
  public TdiSdk(TdiSdkOptions sdkOptions) {
    if (sdkOptions == null) {
      throw new FrameworkRuntimeException("No SDK Options Provided");
    }

    this.sdkOptions = sdkOptions;

    if (this.sdkOptions.plugins == null) {
      this.sdkOptions.plugins = new ArrayList<>();
    }

    /*
     * Validate the parameters
     */
    validateParameters();
  }

  /**
   * Initializes the sdk. It spins up the implementation asynchronously
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiSdkWrapperShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiSdkWrapperShape> init() {

    TdiClassFactoryShape lib = new TdiClassFactory(this.sdkOptions.config);

    return lib.create(this.sdkOptions.platform).thenApply((TdiImplementationShape impl) -> {

      return impl.loadModules(ComponentUtils.getModules()).thenApply(arg -> {

        TdiSdkWrapperShape sdkWrapper = new TdiSdkWrapper();

        if (this.sdkOptions.exposeImpl) {
          ((TdiSdkWrapper) sdkWrapper).setImpl(impl);
        }

        sdkWrapper.setDefaultFlows(TdiSdkApiFactory.buildArgs(impl));
        return sdkWrapper;
      }).thenApply((TdiSdkWrapperShape wrapper) -> {

        /*
         * build the default api flows
         */
        for (Entry<String, TdiFlowArguments> entry : wrapper.getDefaultFlows().entrySet()) {
          wrapper.api(entry.getKey(), impl.buildApiFlow(entry.getValue(), null));
        }
        return wrapper;
      }).thenApply((TdiSdkWrapperShape sdkWrapper) -> {

        impl.loadPlugins(sdkWrapper, this.sdkOptions.plugins);

        return sdkWrapper;
      });

    }).thenCompose(arg -> {
      return arg;
    });
  }

  /**
   * Validates if all the required parameters for instantiation class are
   * present.
   */
  private void validateParameters() {

    if (this.sdkOptions.config == null) {
      throw new FrameworkRuntimeException("No Config Provided");
    }

    if (this.sdkOptions.platform == null) {
      throw new FrameworkRuntimeException("No Platform Provided");
    }
  }

}
