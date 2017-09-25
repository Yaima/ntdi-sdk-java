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

package biz.neustar.tdi.sdk.api;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.sdk.Constants.Components;
import biz.neustar.tdi.sdk.component.TdiSdkExpiresComponent;
import biz.neustar.tdi.sdk.component.TdiSdkJsonWebSignature;
import biz.neustar.tdi.sdk.component.TdiSdkJsonWebTokenComponent;
import biz.neustar.tdi.sdk.component.TdiSdkNonceComponent;
import biz.neustar.tdi.sdk.component.TdiSdkNotBeforeComponent;
import biz.neustar.tdi.sdk.exception.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Container class for default signature workflow.
 *
 */
public class Sign extends BaseApi {

  private static final Logger LOG = LoggerFactory.getLogger(Sign.class);

  /**
   * Constructor.
   * 
   * @param imp
   *          : {@link TdiImplementationShape}
   */
  public Sign(TdiImplementationShape imp) {
    super(imp);
  }

  /**
   * Creates Canonical message and populates payload claim.
   * 
   * @param clientData
   *          : data to be sent as payload
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> handleInit(Object clientData) {
    LOG.trace("Invoking Sign:handleInit");
    return impl.generateMsg(null).thenApply((TdiCanonicalMessageShape msg) -> {
      msg.getClaims().payload = clientData;
      return msg;
    });
  }

  /**
   * Unused at the moment. Meant to do more parsing of the passed in data.
   * 
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> parseRaw(Object msg) {
    /*
     * left for later modifications
     */
    LOG.trace("Invoking Sign:parseRaw");
    return CompletableFuture.completedFuture((TdiCanonicalMessageShape) msg);
  }

  /**
   * Load values in to canonical "claims" key value pairs.
   * 
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> setClaims(Object msg) {

    TdiCanonicalMessageShape tdiMsg = (TdiCanonicalMessageShape) msg;
    LOG.trace("Invoking Sign:setClaims");
    return impl.getPlatform().getKeystore().getSelfKey()
        .thenApply((TdiKeyStructureShape resultKey) -> {

          tdiMsg.getClaims().iss = resultKey.getKeyId();
          return true;
        }).thenApply(arg -> {
          tdiMsg.getClaims().exp = ((TdiSdkExpiresComponent) impl.getModule(Components.EXP))
              .create();
          tdiMsg.getClaims().nbf = ((TdiSdkNotBeforeComponent) impl.getModule(Components.NBF))
              .create();
          tdiMsg.getClaims().jti = ((TdiSdkNonceComponent) impl.getModule(Components.NONCE))
              .create();
          return tdiMsg;
        });
  }

  /**
   * Populates canonical "rawPayload" field with serialized claims.
   * 
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> packClaims(Object msg) {

    LOG.trace("Invoking Sign:packClaims");
    return ((TdiSdkJsonWebTokenComponent) impl.getModule(Components.JWT))
        .packClaims((TdiCanonicalMessageShape) msg);
  }

  /**
   * Adds the key to sign.
   * 
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> setSigners(Object msg) {

    LOG.trace("Invoking Sign:setSigners");

    return impl.getPlatform().getKeystore().getSelfKey().thenApply((TdiKeyStructureShape key) -> {
      ((TdiCanonicalMessageShape) msg).addSigner(key);
      return (TdiCanonicalMessageShape) msg;
    }).exceptionally(throwable -> {
      String errMsg = "Cannot find a SELF key capable of creating a signature.";
      LOG.error(errMsg);
      throw new FrameworkRuntimeException(errMsg);
    });
  }

  /**
   * Signs the 'rawPayload' with the keys from 'signers'.
   * 
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> sign(Object msg) {

    LOG.trace("Invoking Sign:sign");
    return ((TdiSdkJsonWebSignature) impl.getModule(Components.JWS))
        .sign((TdiCanonicalMessageShape) msg);
  }

  /**
   * Handles the canonical "builtMessage" field for final return.
   * 
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> handleReturn(Object msg) {

    LOG.trace("Invoking Sign:handleReturn");
    TdiCanonicalMessageShape tdiMsg = (TdiCanonicalMessageShape) msg;
    CompletableFuture<TdiCanonicalMessageShape> result = new CompletableFuture<>();

    if (StringUtils.isNotBlank(tdiMsg.getBuiltMessage())) {

      result.complete(tdiMsg);

    } else {
      String errMsg = "message could not be generated";
      LOG.error(errMsg);
      result.completeExceptionally(new ApiException(errMsg));
    }

    return result;
  }
}
