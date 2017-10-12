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

package biz.neustar.tdi.fw.plugin;

import biz.neustar.tdi.fw.component.DatastoreDelegate;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import biz.neustar.tdi.fw.exception.ImplementationRequiredException;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract TdiPluginBase class for extension.
 */
public abstract class TdiPluginBase implements TdiPluginBaseFactory {
  /**
   * Implementation associated with the plugin.
   */
  protected TdiImplementationShape impl;

  /**
   * Datastore delegate associated with the plugin.
   */
  protected DatastoreDelegate dataStore;

  protected TdiSdkWrapperShape sdkWrapper;

  private String pluginName;

  /**
   * Default constructor requiring {@link TdiImplementationShape} and
   * {@link TdiSdkWrapperShape} to be associated with it.
   * 
   * @param impl
   *          : Implementation to be associated with.
   * @param sdkWrapper
   *          : {@link TdiSdkWrapperShape} reference of the sdkWrapper.
   * 
   * @throws ImplementationRequiredException
   *           if the implementation is invalid or if is null.
   * 
   */
  public TdiPluginBase(String pluginName, TdiImplementationShape impl,
      TdiSdkWrapperShape sdkWrapper) {
    if (StringUtils.isEmpty(pluginName)) {
      throw new FrameworkRuntimeException("TdiPluginBase name cannot be null");
    }

    this.pluginName = pluginName;

    if (impl == null) {
      throw new ImplementationRequiredException();
    }

    if (sdkWrapper == null) {
      throw new FrameworkRuntimeException("API Required");
    }

    this.impl = impl;
    this.sdkWrapper = sdkWrapper;
    this.dataStore = new DatastoreDelegate(this.getName(), this.impl.getPlatform());
  }

  /**
   * Method to return the delegate instance of the datastore.
   * 
   * @return {@link DatastoreDelegate}
   */
  public DatastoreDelegate getDataStore() {
    return this.dataStore;
  }

  /**
   * Method to be overridden in implementation to provide the initialization of
   * the plugin and complete the {@link CompletableFuture} upon successfull
   * initialization.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link Boolean} with true on
   *         success. false otherwise. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public abstract CompletableFuture<Boolean> init();

  /**
   * Method to return the plugin name.
   * 
   * @return TdiPluginBase name.
   */
  public String getName() {
    return this.pluginName;
  }

  /**
   * Method to validate the data store.
   * 
   * @param checks
   *          : List of String objects to check if available in the
   *          configurations.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link Boolean} with true on
   *         successful validation. false otherwise. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public CompletableFuture<Boolean> validatePluginDataStore(List<String> checks) {
    return this.impl.validateDataStore(this.getName(), checks);
  }

  /**
   * Returns the instance of {@link TdiSdkWrapperShape} associated with this
   * plugin.
   * 
   * @return {@link TdiSdkWrapperShape}
   */
  public TdiSdkWrapperShape getSdkWrapper() {
    return this.sdkWrapper;
  }

  /**
   * Dummy implementation to avoid the classes extending {@link TdiPluginBase}
   * to override or write this in their code. <br>
   * {@inheritDoc}
   */
  @Override
  public TdiPluginBaseFactory newInstance(TdiImplementationShape impl,
      TdiSdkWrapperShape sdkWrapper) {
    return null;
  }
}
