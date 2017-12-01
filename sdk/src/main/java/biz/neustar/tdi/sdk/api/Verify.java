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
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.keystructure.TdiKeyFlagsEnum;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.sdk.Constants.Components;
import biz.neustar.tdi.sdk.component.TdiSdkExpiresComponent;
import biz.neustar.tdi.sdk.component.TdiSdkJsonWebSignature;
import biz.neustar.tdi.sdk.component.TdiSdkJsonWebTokenComponent;
import biz.neustar.tdi.sdk.component.TdiSdkNonceComponent;
import biz.neustar.tdi.sdk.component.TdiSdkNotBeforeComponent;
import biz.neustar.tdi.sdk.component.jws.TdiJwsSignature;
import biz.neustar.tdi.sdk.exception.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Container class for default verify workflow.
 *
 */
public class Verify extends BaseApi {

  private static final Logger LOG = LoggerFactory.getLogger(Verify.class);

  /**
   * Constructor.
   *
   * @param imp
   *          : {@link TdiImplementationShape}
   */
  public Verify(TdiImplementationShape imp) {
    super(imp);
  }

  /**
   * Creates instance of a {@link TdiCanonicalMessageShape} for the raw message
   * string.
   *
   * @param signaturePayload
   *          : raw message string for verification
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> handleInit(Object signaturePayload) {
    LOG.trace("Invoking Verify:handleInit");
    return impl.generateMsg(null).thenApply((TdiCanonicalMessageShape msg) -> {
      ((TdiCanonicalMessage) msg).setReceivedMessage((String) signaturePayload);
      return msg;
    });
  }

  /**
   * Parse the message string according to JWS.
   *
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> unpackEnvelope(Object msg) {
    LOG.trace("Invoking Verify:unpackEnvelope");
    return ((TdiSdkJsonWebSignature) impl.getModule(Components.JWS))
      .unpack((TdiCanonicalMessageShape) msg);
  }

  /**
   * Parse the claim contents according to JWT.
   *
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> unpackClaims(Object msg) {
    LOG.trace("Invoking Verify:unpackClaims");
    return ((TdiSdkJsonWebTokenComponent) impl.getModule(Components.JWT))
      .unpackClaims((TdiCanonicalMessageShape) msg);
  }

  /**
   * Verify that the claims are correct.
   *
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> validateClaims(Object msg) {
    LOG.trace("Invoking Verify:validateClaims");
    TdiCanonicalMessage tdiMsg = (TdiCanonicalMessage) msg;
    CompletableFuture<TdiCanonicalMessageShape> future = new CompletableFuture<>();

    if (tdiMsg.getClaims().iss == null) {
      future.completeExceptionally(new ApiException("No issuer in token"));
      return future;
    }
    if (!((TdiSdkExpiresComponent) impl.getModule(Components.EXP))
        .check(tdiMsg.getClaims().exp)) {
      future.completeExceptionally(new ApiException("Expired Token"));
      return future;
    }
    if (!((TdiSdkNotBeforeComponent) impl.getModule(Components.NBF))
        .check(tdiMsg.getClaims().nbf)) {
      future.completeExceptionally(new ApiException("Token is not yet valid (nbf)"));
      return future;
    }
    return ((TdiSdkNonceComponent) impl.getModule(Components.NONCE))
      .check((String) tdiMsg.getClaims().jti)
      .thenCompose((Boolean check) -> {
        if (check) {
          future.complete(tdiMsg);
        }
        else {
          future.completeExceptionally(new ApiException("Bad Nonce"));
        }
        return future;
      });
  }


  /**
   * Parses signatures and collects available keys to verify.
   *
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> prepSignatures(Object msg) {
    LOG.trace("Invoking Verify:prepSignatures");
    TdiCanonicalMessageShape tdiMsg = (TdiCanonicalMessageShape) msg;
    List<CompletableFuture<TdiKeyStructureShape>> keys = new ArrayList<>();
    Map<Object, CompletableFuture<TdiKeyStructureShape>> keysWithKid = new HashMap<>();
    CompletableFuture<TdiCanonicalMessageShape> finalFuture = new CompletableFuture<>();
    List<Object> signaturesToVerify = tdiMsg.getSignaturesToVerify();

    for (int loopIndex = 0; loopIndex < signaturesToVerify.size(); loopIndex++) {
      TdiJwsSignature signature = (TdiJwsSignature) signaturesToVerify.get(loopIndex);
      String kid = signature.parsedHeader.kid;
      if (StringUtils.isNotBlank(kid)) {
        CompletableFuture<TdiKeyStructureShape> tdiKey = impl.getPlatform().getKeystore().getKey(kid)
          .thenApply((TdiKeyStructureShape key) -> {
            return key;
          })
          .exceptionally(throwable -> {
            return null;
          });
        keys.add(tdiKey);
        keysWithKid.put(signature, tdiKey);
      }
    }

    return CompletableFuture.allOf(keys.toArray(new CompletableFuture<?>[0]))
      .thenCompose((arg) -> {
        Object[] fcfs = new Object[2];
        for (Entry<Object, CompletableFuture<TdiKeyStructureShape>> entry : keysWithKid.entrySet()) {
          try {
            TdiKeyStructureShape keyVal = entry.getValue().get();
            if ((null != keyVal) && (null != keyVal.getRoleFlag())) {
              if (TdiKeyFlagsEnum.ROLE_F_C.getNumber()
                  .equals(keyVal.getRoleFlag())) {
                fcfs[0] = entry.getKey();
              }
              else if (TdiKeyFlagsEnum.ROLE_F_S.getNumber()
                  .equals(keyVal.getRoleFlag())) {
                fcfs[1] = entry.getKey();
              }
            }
          } catch (CancellationException | InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage());
          }
        }

        if ((fcfs[0] != null) && (fcfs[1] != null)) {
          tdiMsg.getSignaturesToVerify().clear();
          tdiMsg.addSignatureToVerify(fcfs[0]);
          tdiMsg.addSignatureToVerify(fcfs[1]);
          finalFuture.complete(tdiMsg);
        }
        else {
          String errMsg = "Signatures do not have roles: F_C:"
              + ((fcfs[0] != null) ? " found" : "missing") + " F_S:"
              + ((fcfs[1] != null) ? " found" : "missing");
          finalFuture.completeExceptionally(new ApiException(errMsg));
        }
        return finalFuture;
      });
  }

  /**
   * Verifies signatures.
   *
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> verifySignatures(Object msg) {
    LOG.trace("Invoking Verify:verifySignatures: " + msg);
    return ((TdiSdkJsonWebSignature) impl.getModule(Components.JWS))
      .verify((TdiCanonicalMessageShape) msg);
  }

  /**
   * Updates nonce cache.
   *
   * @param msg
   *          : received {@link TdiCanonicalMessageShape} instance
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> afterVerify(Object msg) {
    LOG.trace("Invoking Verify:afterVerify");
    TdiCanonicalMessage tdiMsg = (TdiCanonicalMessage) msg;
    return ((TdiSdkNonceComponent) impl.getModule(Components.NONCE))
      .burn(tdiMsg.getClaims().jti)
      .thenApply(arg -> {
        LOG.trace("Nonce successfully burned");
        return tdiMsg;
      });
  }

  /**
   * Returns the authenticated payload.
   *
   * @param msg
   *          received {@link TdiCanonicalMessageShape} instance
   *
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link String}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<String> handleReturn(Object msg) {
    LOG.trace("Invoking Verify:handleReturn");
    TdiCanonicalMessage tdiMsg = (TdiCanonicalMessage) msg;
    return CompletableFuture.completedFuture((String)tdiMsg.getClaims().payload);
  }

}
