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

package biz.neustar.tdi.platform;

import biz.neustar.tdi.fw.platform.TdiPlatformBase;
import biz.neustar.tdi.fw.platform.facet.crypto.TdiPlatformCryptoShape;
import biz.neustar.tdi.fw.platform.facet.data.TdiPlatformDataShape;
import biz.neustar.tdi.fw.platform.facet.keys.TdiPlatformKeysShape;
import biz.neustar.tdi.fw.platform.facet.time.TdiPlatformTimeShape;
import biz.neustar.tdi.fw.platform.facet.utils.TdiPlatformUtilsShape;
import biz.neustar.tdi.platform.exception.PlatformRuntimeException;
import biz.neustar.tdi.platform.facet.CryptFacet;
import biz.neustar.tdi.platform.facet.DataStoreFacet;
import biz.neustar.tdi.platform.facet.KeystoreFacet;
import biz.neustar.tdi.platform.facet.TimeFacet;
import biz.neustar.tdi.platform.facet.UtilFacet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link Platform} class.
 * 
 */
public class Platform extends TdiPlatformBase {
  private TdiPlatformKeysShape keystore;
  private TdiPlatformUtilsShape util;
  private TdiPlatformTimeShape time;
  private TdiPlatformCryptoShape crypto;
  private TdiPlatformDataShape datastore;

  /**
   * Constructor for Platform class.
   * 
   * @param config
   *          : Configuration Key-Value Map
   */
  @SuppressWarnings("unchecked")
  public Platform(Map<String, Object> config) {
    super((Map<String, Object>) config.get(Constants.PLATFORM_CONFIG_KEY_PLATFORM));

    // Init member objects and store the instance
    time = new TimeFacet(this);
    util = new UtilFacet(this);
    keystore = new KeystoreFacet(this);
    crypto = new CryptFacet(this);
    datastore = new DataStoreFacet(this);
  }

  /**
   * Method to get {@link TdiPlatformKeysShape} class object.
   * 
   * @return {@link TdiPlatformKeysShape} class object.
   */
  @Override
  public TdiPlatformKeysShape getKeystore() {
    return keystore;
  }

  /**
   * Method to get {@link TdiPlatformCryptoShape} class object.
   * 
   * @return {@link TdiPlatformCryptoShape} class object.
   */
  @Override
  public TdiPlatformCryptoShape getCrypto() {
    return crypto;
  }

  /**
   * Method to get {@link TdiPlatformDataShape} class object.
   * 
   * @return {@link TdiPlatformDataShape} class object.
   */
  @Override
  public TdiPlatformDataShape getDataStore() {
    return datastore;
  }

  /**
   * Method to get {@link TdiPlatformTimeShape} class object.
   * 
   * @return {@link TdiPlatformTimeShape} class object.
   */
  @Override
  public TdiPlatformTimeShape getTime() {
    return time;
  }

  /**
   * Method to get {@link TdiPlatformUtilsShape} class object.
   * 
   * @return {@link TdiPlatformUtilsShape} class object.
   */
  @Override
  public TdiPlatformUtilsShape getUtils() {
    return util;
  }

  /**
   * Method initialize platform class.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void after all the components are
   *         successfully initialized. <br>
   *         <b>Completed Exceptionally</b>: {@link PlatformRuntimeException} if
   *         any of the components fail to initialize.
   */
  @Override
  public CompletableFuture<Void> init() {

    List<CompletableFuture<Void>> queue = new ArrayList<>();

    queue.add(keystore.init());
    queue.add(util.init());
    queue.add(time.init());
    queue.add(crypto.init());
    queue.add(datastore.init());

    return CompletableFuture.allOf(queue.toArray(new CompletableFuture<?>[0]));
  }

}
