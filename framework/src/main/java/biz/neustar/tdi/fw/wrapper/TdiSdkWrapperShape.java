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

package biz.neustar.tdi.fw.wrapper;

import biz.neustar.tdi.fw.implementation.TdiFlowArguments;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.plugin.TdiPluginBase;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Interface for API.
 */
public interface TdiSdkWrapperShape {
  /**
   * Returns the default flows of this Wrapper.
   * 
   * @return {@link Map}&lt;String, {@link TdiFlowArguments}&gt;
   */
  public Map<String, TdiFlowArguments> getDefaultFlows();

  /**
   * Sets the default flows of this wrapper.
   * 
   * @param flows
   *          : {@link Map}&lt;String, {@link TdiFlowArguments}&gt; 
   */
  public void setDefaultFlows(Map<String, TdiFlowArguments> flows);

  /**
   * Returns the instance of the API associated with the apiName.
   * 
   * @param apiName
   *          : Name of API to be retrieved.
   * @param <T>
   *          : Class type template
   * @param <R>
   *          : Class type template
   * 
   * @return Instance of the API as Object. Needs type-casting.
   */
  public <T, R> Function<T, CompletableFuture<R>> api(String apiName);

  /**
   * Set an API object against apiName
   * 
   * @param apiName
   *          : Name of the sdkWrapper.
   * @param api
   *          : API Object.
   * @param <T>
   *          : Class type template
   * @param <R>
   *          : Class type template
   * 
   */
  public <T, R> void api(String apiName, Function<T, CompletableFuture<R>> api);

  /**
   * Returns the instance of {@link TdiPluginBase} associated with plugin name.
   * 
   * @param pluginName
   *          :Name of the {@link TdiPluginBase} to be retrieved.
   * @return {@link TdiPluginBase}
   */
  public TdiPluginBase plugin(String pluginName);

  /**
   * Store the plugin which can be retrieve later using a name matching
   * {@link TdiPluginBase#getName()}
   * 
   * @param plugin
   *          {@link TdiPluginBase} object to be stored.
   */
  public void plugin(TdiPluginBase plugin);

  /**
   * Return a map of plugins with plugin names as their key.
   * 
   * @return Map&lt;String, {@link TdiPluginBase}&gt;
   */
  public Map<String, TdiPluginBase> plugins();

  /**
   * Returns {@link TdiImplementationShape} instance if exposed in
   * {@link TdiSdkWrapperShape}.
   * 
   * @return {@link TdiImplementationShape}
   */
  public TdiImplementationShape getImpl();

  /**
   * Sets the {@link TdiImplementationShape} instance.
   * 
   * @param impl
   *          : {@link TdiImplementationShape}
   */
  void setImpl(TdiImplementationShape impl);
}
