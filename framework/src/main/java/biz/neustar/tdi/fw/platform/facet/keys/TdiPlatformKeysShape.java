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

package biz.neustar.tdi.fw.platform.facet.keys;

import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.platform.facet.TdiPlatformFacetShape;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Platform facet: Abstracted Keystore.
 */
public interface TdiPlatformKeysShape extends TdiPlatformFacetShape {
  /**
   * Returns the instance of {@link TdiKeyStructureShape} initialized with key
   * against the key id, based on the platform implementation.
   * 
   * @param kid
   *          : Id of the key to be returned.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiKeyStructureShape}
   *         instance with key data. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public CompletableFuture<TdiKeyStructureShape> getKey(String kid);

  /**
   * Returns a list of {@link TdiKeyStructureShape} stored on the platform
   * keystore, based on the platform implementation.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>:
   *         {@link List}&lt;{@link TdiKeyStructureShape}&gt; all the keys in
   *         the keystore. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public CompletableFuture<List<TdiKeyStructureShape>> getKeys();

  /**
   * Returns the self {@link TdiKeyStructureShape} (Identity credentials), based
   * on the platform implementation.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiKeyStructureShape}
   *         instance with SELF key data. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of
   *         failure.
   */
  public CompletableFuture<TdiKeyStructureShape> getSelfKey();

  /**
   * Returns the key assocaited with the role and fleetid, based on the platform
   * implementation.
   * 
   * @param role
   *          : Key role.
   * @param fleetId
   *          : Fleet ID.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiKeyStructureShape} <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiKeyStructureShape> getKeyByRole(Integer role, String fleetId);

  /**
   * Returns the public key data associated with the key.
   * 
   * @param key
   *          : Key structure. Depends on implementor to define the structure.
   * 
   * @return Public key.
   */
  public Object getPublicPem(Object key);

  /**
   * Method to generate a new {@link TdiKeyStructureShape} with flags, kid, and
   * fleet id, based on the platform implementation.
   * 
   * @param flags
   *          : Flags to be associated with new key.
   * @param kid
   *          : Key ID.
   * @param fleetId
   *          : Fleet ID.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: new {@link TdiKeyStructureShape} <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiKeyStructureShape> generateKey(Integer flags, String kid,
      String fleetId);

  /**
   * Stores the passed key in the keystore, based on the platform
   * implementation.
   * 
   * @param key
   *          : Key to be stored.
   * @param flags
   *          : Flags of the key.
   * @param fleetId
   *          : Fleet ID.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiKeyStructureShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiKeyStructureShape> setKey(Object key, Integer flags, String fleetId);

  /**
   * Stores the passed provisioned key, based on the platform implementation.
   * 
   * @param key
   *          : Provisioned key to be stored.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiKeyStructureShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiKeyStructureShape> setKeyFromProvision(Object key);

  /**
   * Method to forget the key associated with the kid, based on the platform
   * implementation.
   * 
   * @param kid
   *          : Id of the key to be forgotten.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<Void> forgetKey(String kid);

  /**
   * Method to persist the store, based on the platform implementation.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<Void> saveStore();
}
