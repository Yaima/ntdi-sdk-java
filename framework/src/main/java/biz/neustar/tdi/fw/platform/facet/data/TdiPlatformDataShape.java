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

package biz.neustar.tdi.fw.platform.facet.data;

import biz.neustar.tdi.fw.platform.facet.TdiPlatformFacetShape;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Platform facet: Abstracted Datastore.
 */
public interface TdiPlatformDataShape extends TdiPlatformFacetShape {
  /**
   * Method to get value of a key from the store, based on the platform
   * implementation.
   * 
   * @param storeName
   *          : Store name to be looked into.
   * @param key
   *          : Key to be looked up.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Data against key. (Type casting
   *         required). <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public CompletableFuture<?> get(String storeName, String key);

  /**
   * Method to set value of a key in the store, based on the platform
   * implementation.
   * 
   * @param storeName
   *          : Store name to be updated.
   * @param key
   *          : Key to be set/updated.
   * @param value
   *          : Value to be set/updated.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public CompletableFuture<Void> set(String storeName, String key, Object value);

  /**
   * Method to return all the keys in the store, based on the platform
   * implementation.
   * 
   * @param storeName
   *          : Name of the store to be looked into.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link List}&lt;{@link String}&gt;
   *         with keys in the store. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public CompletableFuture<List<String>> keys(String storeName);

  /**
   * Method to create a store, based on the platform implementation.
   * 
   * @param storeName
   *          : Store to be created.
   * @param value
   *          : Initial value to be added to the store.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Map of store if store already exists. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<?> createStore(String storeName, Map<String, Object> value);

  /**
   * Method to delete a store, based on the platform implementation.
   * 
   * @param storeName
   *          : Store to be deleted.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<Void> deleteStore(String storeName);

  /**
   * Method to drop a key from the store, based on the platform implementation.
   * 
   * @param storeName
   *          : Store to be updated.
   * @param key
   *          : Key to be dropped.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<Void> drop(String storeName, String key);
}
