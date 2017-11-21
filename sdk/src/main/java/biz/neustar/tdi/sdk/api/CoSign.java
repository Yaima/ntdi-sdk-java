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

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessage;
import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import biz.neustar.tdi.fw.exception.InvalidFormatException;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.utils.Utils;
import biz.neustar.tdi.sdk.Constants.Components;
import biz.neustar.tdi.sdk.component.TdiSdkJsonWebSignature;
import biz.neustar.tdi.sdk.component.jws.TdiJws;
import biz.neustar.tdi.sdk.component.jws.TdiJwsSignature;
import biz.neustar.tdi.sdk.exception.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Container class for default co-signature workflow.
 *
 */
public class CoSign extends BaseApi {

  private static final Logger LOG = LoggerFactory.getLogger(CoSign.class);

  /**
   * Constructor.
   *
   * @param imp
   *          : {@link TdiImplementationShape} instance
   */
  public CoSign(TdiImplementationShape imp) {
    super(imp);

  }

  /**
   * Create Canonical message &amp; populate payload claim and signatures.
   *
   * @param clientJwsString
   *          : received jws string
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape} <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> handleInit(Object clientJwsString) {
    LOG.trace("Invoking CoSign:handleInit");
    return impl.generateMsg(null).thenCompose((TdiCanonicalMessageShape msg) -> {
      TdiCanonicalMessage tdiMsg = (TdiCanonicalMessage) msg;
      CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();
      try {
        TdiJws jwsMessage = Utils.jsonToObject((String) clientJwsString, TdiJws.class);
        tdiMsg.setRawPayload(jwsMessage.payload);

        for (TdiJwsSignature signature : jwsMessage.signatures) {
          tdiMsg.addHeldSignature(signature);
        }

        future.complete(tdiMsg);
      }
      catch (Exception e) {
        future.completeExceptionally(new ApiException("Error parsing jws string"));
      }
      return future;
    });
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

    LOG.trace("Invoking CoSign:setSigners");

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
   * Co-signs the message.
   *
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> sign(Object msg) {

    LOG.trace("Invoking CoSign:sign");
    return ((TdiSdkJsonWebSignature) impl.getModule(Components.JWS))
        .sign((TdiCanonicalMessageShape) msg);
  }

  /**
   * Update the built message with heldSignatures.
   *
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> handleReturn(Object msg) {

    LOG.trace("Invoking CoSign:handleReturn");

    CompletableFuture<TdiCanonicalMessageShape> result = new CompletableFuture<>();
    TdiCanonicalMessageShape tdiMsg = (TdiCanonicalMessageShape) msg;

    if (StringUtils.isNotBlank(tdiMsg.getBuiltMessage())) {
      try {
        TdiJws jwsMessage = Utils.jsonToObject(tdiMsg.getBuiltMessage(), TdiJws.class);
        List<Object> heldSignatures = tdiMsg.getHeldSignatures();
        Iterator<Object> iter = heldSignatures.iterator();
        while (iter.hasNext()) {
          jwsMessage.signatures.add((TdiJwsSignature) iter.next());
        }

        tdiMsg.setBuiltMessage(Utils.objectToJson(jwsMessage));
        result.complete(tdiMsg);
      } catch (InvalidFormatException exp) {
        result.completeExceptionally(exp);
      }
    } else {
      String errMsg = "message could not be generated";
      LOG.error(errMsg);
      result.completeExceptionally(new ApiException(errMsg));
    }

    return result;
  }
}
