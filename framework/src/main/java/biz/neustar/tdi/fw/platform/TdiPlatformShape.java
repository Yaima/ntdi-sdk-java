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

package biz.neustar.tdi.fw.platform;

import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import biz.neustar.tdi.fw.platform.facet.TdiPlatformFacetShape;
import biz.neustar.tdi.fw.platform.facet.crypto.TdiPlatformCryptoShape;
import biz.neustar.tdi.fw.platform.facet.data.TdiPlatformDataShape;
import biz.neustar.tdi.fw.platform.facet.keys.TdiPlatformKeysShape;
import biz.neustar.tdi.fw.platform.facet.time.TdiPlatformTimeShape;
import biz.neustar.tdi.fw.platform.facet.utils.TdiPlatformUtilsShape;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for platform. The {@link TdiPlatformFacetShape} facets are composed into a
 * framework-wide object that is responsible for functions that have
 * platform-specific implementations.
 */
public interface TdiPlatformShape extends TdiPlatformShapeFactory {
  /**
   * Method to return the configurations associated with the plugin.
   * 
   * @return Map&lt;String, Object&gt; of configurations.
   */
  public Map<String, Object> getConfig();

  /**
   * Method to check if the given set of keys are present in the configurations
   * returned by {@link TdiPlatformShape#getConfig()}
   * 
   * @param keys
   *          : Set of String keys to check.
   * 
   * @return true if all the keys are contained in the configuration. False
   *         otherwise.
   * 
   * @throws FrameworkRuntimeException
   *           if any of the keys are missing.
   */
  public boolean checkConfig(Set<String> keys);

  /**
   * Returns the instance of {@link TdiPlatformKeysShape} associated with this platform.
   * 
   * @return {@link TdiPlatformKeysShape}
   */
  public TdiPlatformKeysShape getKeystore();

  /**
   * Returns the instance of {@link TdiPlatformCryptoShape} associated with this platform.
   * 
   * @return {@link TdiPlatformCryptoShape}
   */
  public TdiPlatformCryptoShape getCrypto();

  /**
   * Returns the instance of {@link TdiPlatformDataShape} associated with this platform.
   * 
   * @return {@link TdiPlatformDataShape}
   */
  public TdiPlatformDataShape getDataStore();

  /**
   * Returns the instance of {@link TdiPlatformTimeShape} associated with this platform.
   * 
   * @return {@link TdiPlatformTimeShape}
   */
  public TdiPlatformTimeShape getTime();

  /**
   * Returns the instance of {@link TdiPlatformUtilsShape} associated with this platform.
   * 
   * @return {@link TdiPlatformUtilsShape}
   */
  public TdiPlatformUtilsShape getUtils();

  /**
   * Method to perform initialization tasks and complete the CompletableFuture
   * object upon successful initialization.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<Void> init();
}
