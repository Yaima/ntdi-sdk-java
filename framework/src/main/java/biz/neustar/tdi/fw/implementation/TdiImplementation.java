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

package biz.neustar.tdi.fw.implementation;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessage;
import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.classfactory.TdiClassFactory;
import biz.neustar.tdi.fw.component.TdiComponentShape;
import biz.neustar.tdi.fw.component.TdiComponentShapeFactory;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.fw.platform.TdiPlatformShapeFactory;
import biz.neustar.tdi.fw.plugin.TdiPluginBase;
import biz.neustar.tdi.fw.plugin.TdiPluginBaseFactory;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * The base implementation that is returned by
 * {@link TdiClassFactory#create(TdiPlatformShapeFactory)}.
 *
 */
public class TdiImplementation implements TdiImplementationShape {

  private static final Logger LOG = LoggerFactory.getLogger(TdiImplementation.class);
  private static final String FWSTORE = "ntdifw";
  private static final String FLEETS = "fleets";

  /**
   * Canonical message iterator.
   *
   */
  private int canonicalTracker = 0;

  /**
   * TdiComponent reference key/value store.
   *
   */
  private Map<String, TdiComponentShape> modules;

  /**
   * Configuration for datastore defaults.
   */
  private Map<String, Object> config;

  /**
   * Platform reference.
   */
  private TdiPlatformShape platform;

  /**
   * Instantiates an object.
   *
   * @param conf
   *          the configuration
   * @param pfFactory
   *          reference of class implementing {@link TdiPlatformShape}
   */
  public TdiImplementation(Map<String, Object> conf, TdiPlatformShapeFactory pfFactory) {
    config = conf;
    platform = (TdiPlatformShape) pfFactory.newInstance(conf);
    modules = new HashMap<>();
  }

  public void dumpDebug() {
  }


  /**
   * Sets up the global datastore.
   *
   * @param storeMap
   *          : Store map.
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link Boolean} with true on
   *         success. false otherwise. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   *
   */
  public CompletableFuture<Boolean> setupTdiStore(Map<String, Object> storeMap) {

    LOG.debug("Loading NTDI datastore...");

    List<CompletableFuture<?>> queue = new ArrayList<>();
    List<String> fleets;

    if (!storeMap.containsKey(FLEETS)) {
      fleets = new ArrayList<>();
      queue.add(platform.getDataStore().set(FWSTORE, FLEETS, fleets));
    }

    CompletableFuture<Void> allInQueue = CompletableFuture
        .allOf(queue.toArray(new CompletableFuture<?>[0]));

    return allInQueue.thenApply((arg) -> {
      return true;
    });
  }

  /**
   * Pass-through to platform for special-case key access.
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiKeyStructureShape} with
   *         SELF Key details. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public CompletableFuture<TdiKeyStructureShape> getSelfKey() {
    return platform.getKeystore().getSelfKey();
  }

  @Override
  public CompletableFuture<Boolean> loadModule(String moduleName,
      TdiComponentShapeFactory componentRef) {
    TdiComponentShape component = (TdiComponentShape) componentRef.newInstance(moduleName, this);
    modules.put(moduleName, component);
    return component.init().thenApply((arg) -> {
      return true;
    });
  }

  @Override
  public CompletableFuture<Void> loadModules(Map<String, TdiComponentShapeFactory> modules) {

    List<CompletableFuture<Boolean>> queue = new ArrayList<>();

    for (Entry<String, TdiComponentShapeFactory> entry : modules.entrySet()) {
      queue.add(loadModule(entry.getKey(), entry.getValue()));
    }

    return CompletableFuture.allOf(queue.toArray(new CompletableFuture<?>[0]));
  }

  @Override
  public CompletableFuture<TdiSdkWrapperShape> loadPlugins(TdiSdkWrapperShape sdkWrapper, List<TdiPluginBaseFactory> plugins) {
    LOG.trace("Loading plugins...");
    List<CompletableFuture<?>> queue = new ArrayList<>();

    @SuppressWarnings("unchecked")
    CompletableFuture<Object> dataPromise = platform.getDataStore().createStore(FWSTORE, null)
        .thenApply((arg) -> {
          return setupTdiStore((Map<String, Object>) arg);
        });
    queue.add(dataPromise);

    CompletableFuture<Boolean> pluginPromise;

    if (!plugins.isEmpty()) {

      for (TdiPluginBaseFactory pluginRef : plugins) {
        TdiPluginBase plugin = (TdiPluginBase) pluginRef.newInstance(this, sdkWrapper);
        sdkWrapper.plugin(plugin);
        pluginPromise = platform.getDataStore().createStore(plugin.getName(), null)
            .thenCompose((arg) -> {
              return sdkWrapper.plugin(plugin.getName()).init();
            });
        queue.add(pluginPromise);
      }
    }

    return CompletableFuture.allOf(queue.toArray(new CompletableFuture<?>[0])).thenApply((arg) -> {
      return sdkWrapper;
    });
  }

  @Override
  public TdiPlatformShape getPlatform() {
    return this.platform;
  }

  @Override
  public Map<String, Object> getConfig() {
    return this.config;
  }

  @Override
  public CompletableFuture<Boolean> validateDataStore(String storeName, List<String> checks) {

    List<CompletableFuture<?>> queue = new ArrayList<>();

    for (String check : checks) {
      CompletableFuture<?> result = platform.getDataStore().get(storeName, check)
          .handle((arg, throwable) -> {
            if (arg != null) {
              return CompletableFuture.completedFuture(arg);
            } else {
              @SuppressWarnings("unchecked")
              Map<String, Object> storeInConfig = (Map<String, Object>) config.get(storeName);
              if (storeInConfig != null && storeInConfig.containsKey(check)) {
                return platform.getDataStore().set(storeName, check, storeInConfig.get(check));
              }
              String err = "No config for " + storeName + "::" + check + ", nor is there a default value.";
              LOG.warn(err);

              CompletableFuture<?> ret = new CompletableFuture<>();
              ret.completeExceptionally(new FrameworkRuntimeException(err));
              return(ret);
            }
          }).thenCompose((finalresult) -> {
            return finalresult;
          });

      queue.add(result);
    }

    return CompletableFuture.allOf(queue.toArray(new CompletableFuture<?>[0])).thenApply((arg) -> {
      return true;
    });
  }

  @Override
  public TdiComponentShape getModule(String moduleName) {
    return modules.get(moduleName);
  }

  @Override
  public CompletableFuture<TdiCanonicalMessageShape> generateMsg(String fleetId) {

    TdiCanonicalMessageShape msg = new TdiCanonicalMessage(canonicalTracker++);

    return getSelfKey().thenApply((TdiKeyStructureShape key) -> {
      ((TdiCanonicalMessage) msg).setCurrentProject((fleetId != null) ? fleetId : key.getFleetId());
      return msg;
    }).exceptionally(throwable -> {
      String errMsg = "Cannot find a SELF key capable of creating a signature.";
      LOG.error(errMsg);
      throw new FrameworkRuntimeException(errMsg);
    });
  }

  @Override
  public <T, R> Function<T, CompletableFuture<R>> buildApiFlow(TdiFlowArguments originalFlow,
      TdiFlowArguments otherFlow) {

    Function<T, CompletableFuture<R>> apiFunc = new Function<T, CompletableFuture<R>>() {

      @SuppressWarnings("unchecked")
      @Override
      public CompletableFuture<R> apply(T data) {
        return (CompletableFuture<R>) buildFlows(data, originalFlow, otherFlow);
      }

      /**
       * Build the api flows.
       *
       * @param data
       *          : the data to be passed to built api flows
       * @param originalFlow
       *          : the predefined api flows
       * @param otherFlow
       *          : the overriding and additional api flows
       *
       * @return the built api flow as a {@link CompletableFuture}
       */
      private CompletableFuture<? extends Object> buildFlows(T data, TdiFlowArguments originalFlow,
          TdiFlowArguments otherFlow) {

        // handle the scenario if otherFlow is null
        TdiFlowArguments newFlow;
        if (otherFlow == null) {
          newFlow = new TdiFlowArguments();
        } else {
          newFlow = otherFlow;
        }

        CompletableFuture<? extends Object> currentFuture = CompletableFuture.completedFuture(data);

        for (Entry<String, Function<Object, CompletableFuture<? extends Object>>> entry :
            originalFlow.getFlowMap().entrySet()) {

          if (newFlow.getFlowMap().containsKey(entry.getKey())
              && newFlow.getOverrideSteps().contains(entry.getKey())) {

            currentFuture = currentFuture.thenCompose((msg) -> {
              return newFlow.getFlowMap().get(entry.getKey()).apply(msg);
            });

          } else if (newFlow.getFlowMap().containsKey(entry.getKey())
              && (!newFlow.getOverrideSteps().contains(entry.getKey()))) {
            currentFuture = currentFuture.thenCompose((msg) -> {
              return entry.getValue().apply(msg);
            }).thenCompose((msg) -> {
              return newFlow.getFlowMap().get(entry.getKey()).apply(msg);
            });
          } else {
            currentFuture = currentFuture.thenCompose((msg) -> {
              return entry.getValue().apply(msg);
            });
          }
        }
        return currentFuture;
      }
    };
    return apiFunc;
  }
}
