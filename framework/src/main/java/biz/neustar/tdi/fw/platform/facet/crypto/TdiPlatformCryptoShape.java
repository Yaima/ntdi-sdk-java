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

package biz.neustar.tdi.fw.platform.facet.crypto;

import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.platform.facet.TdiPlatformFacetShape;

import java.util.concurrent.CompletableFuture;

/**
 * Platform facet: Abstracted Crypto.
 */
public interface TdiPlatformCryptoShape extends TdiPlatformFacetShape {
  /**
   * Method to sign a payload with the provided {@link TdiKeyStructureShape}
   * key, based on the platform implementation.
   * 
   * @param key
   *          : {@link TdiKeyStructureShape} key to be used for signing.
   * @param payload
   *          : Payload to be signed.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Computed signature of the sign action <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<String> sign(TdiKeyStructureShape key, String payload);

  /**
   * Method to verify a payload against a signature using
   * {@link TdiKeyStructureShape} key, based on the platform implementation.
   * 
   * @param key
   *          : {@link TdiKeyStructureShape} key to be used for verifying.
   * @param payload
   *          : Payload to be verified.
   * @param signature
   *          : Signature to be verified.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link Boolean} true if verified. false otherwise. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<Boolean> verify(TdiKeyStructureShape key, String payload,
      String signature);

  /**
   * Method to encrypt
   * 
   * @return Empty {@link CompletableFuture} object.
   */
  public CompletableFuture<Void> encrypt();

  /**
   * Method to decrypt.
   * 
   * @return Empty {@link CompletableFuture} object.
   */
  public CompletableFuture<Void> decrypt();
}
