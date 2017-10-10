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

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.component.TdiComponentShape;
import biz.neustar.tdi.fw.component.TdiComponentShapeFactory;
import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.fw.plugin.TdiPluginBase;
import biz.neustar.tdi.fw.plugin.TdiPluginBaseFactory;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Interface for the Implementation.
 */
public interface TdiImplementationShape {
  /**
   * Return instance of {@link TdiPlatformShape} associated with this
   * implementation.
   * 
   * @return {@link TdiPlatformShape}
   */
  public TdiPlatformShape getPlatform();

  /**
   * Returns the configurations associated with this implementation.
   * 
   * @return Map&lt;String,Object&gt;
   */
  public Map<String, Object> getConfig();

  /**
   * Method to validate the data store.
   * 
   * @param storeName
   *          : Store name under which the checks needs to be checked.
   * @param checks
   *          : List of String objects to check if available in the
   *          configurations.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link Boolean} with true on
   *         success. false otherwise. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public CompletableFuture<Boolean> validateDataStore(String storeName, List<String> checks);

  /**
   * Method to load the {@link TdiComponentShape} modules into the
   * implementation.
   * 
   * @param moduleName
   *          : Module name to be initialized with.
   * @param component
   *          : {@link TdiComponentShapeFactory} TdiComponent to be loaded.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link Boolean} with true if module
   *         loaded successfully. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public CompletableFuture<Boolean> loadModule(String moduleName,
      TdiComponentShapeFactory component);

  /**
   * Method to load given modules
   * 
   * @param modules
   *          : map with key as module name to be initialized with, and value as
   *          {@link TdiComponentShapeFactory} TdiComponent to be loaded.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void after all the modules loaded
   *         successfully. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public CompletableFuture<Void> loadModules(Map<String, TdiComponentShapeFactory> modules);

  /**
   * Returns the instance of the {@link TdiComponentShape} associated with the
   * passed moduleName.
   * 
   * @param moduleName
   *          : Name of the module to be retrieved.
   * 
   * @return {@link TdiComponentShape} instance.
   */
  public TdiComponentShape getModule(String moduleName);

  /**
   * Method to load the {@link TdiPluginBase}.
   * 
   * @param sdkWrapper
   *          : {@link TdiSdkWrapperShape}
   * @param plugins
   *          : {@link TdiPluginBaseFactory} instance.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiSdkWrapperShape} instance
   *         extended with plugins. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public CompletableFuture<TdiSdkWrapperShape> loadPlugins(TdiSdkWrapperShape sdkWrapper,
      List<TdiPluginBaseFactory> plugins);

  /**
   * Method to generate a new {@link TdiCanonicalMessageShape} object
   * initialized with fleetId.
   * 
   * @param fleetId
   *          : Fleet ID.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: new {@link TdiCanonicalMessageShape} instance. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> generateMsg(String fleetId);

  /**
   * Method to build the api flows.
   * 
   * @param <T>
   *          : input for the function
   * @param <R>
   *          : output from the function
   * @param originalFlow
   *          : an object containing a {@link LinkedHashMap} with the steps and
   *          their respective method references.
   * @param otherFlow
   *          : an object containing a map with the steps and their respective
   *          method references. It also contains a list of overriding steps.
   * 
   * @return a function reference, when invoked will apply the built api flow on
   *         data
   */
  public <T, R> Function<T, CompletableFuture<R>> buildApiFlow(TdiFlowArguments originalFlow,
      TdiFlowArguments otherFlow);

}
