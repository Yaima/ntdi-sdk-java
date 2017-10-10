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

package biz.neustar.tdi.fw.component;

import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.fw.platform.facet.data.TdiPlatformDataShape;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Delegate class for allowing access to basic methods of {@link TdiPlatformDataShape}
 * through {@link TdiPlatformShape}.
 */
public class DatastoreDelegate {
  String storeName;
  TdiPlatformShape platform;

  public DatastoreDelegate(String storeName, TdiPlatformShape platform) {
    this.storeName = storeName;
    this.platform = platform;
  }

  /**
   * Method to delegate call to {@link TdiPlatformShape}'s {@link TdiPlatformDataShape} to return
   * the value for key using this store name.
   * 
   * @param key
   *          : Key
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Data corresponding to key. (Type casting required). <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<?> get(String key) {
    return this.platform.getDataStore().get(this.storeName, key);
  }

  /**
   * Method to delegate call to {@link TdiPlatformShape}'s {@link TdiPlatformDataShape} to set the
   * value for key using this store name.
   * 
   * @param key
   *          : Key
   * @param value
   *          : value to be set
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<Void> set(String key, Object value) {
    return this.platform.getDataStore().set(this.storeName, key, value);
  }

  /**
   * Method to delegate call to {@link TdiPlatformShape}'s {@link TdiPlatformDataShape} to retrieve
   * all keys using this store name.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link List} or keys. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<List<String>> keys() {
    return this.platform.getDataStore().keys(this.storeName);
  }

  /**
   * Method to delegate call to {@link TdiPlatformShape}'s {@link TdiPlatformDataShape} to drop a
   * key using this store name.
   * 
   * @param key
   *          : Key to drop
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<Void> drop(String key) {
    return this.platform.getDataStore().drop(this.storeName, key);
  }
}
