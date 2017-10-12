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

package biz.neustar.tdi.sdk.component;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessage;
import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.canonicalmessage.TdiClaims;
import biz.neustar.tdi.fw.component.TdiComponent;
import biz.neustar.tdi.fw.exception.InvalidFormatException;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * This component is the parser/packer layer for messages with the JWT wire
 * format.
 */
public class TdiSdkJsonWebTokenComponent extends TdiComponent {
  private static final Logger LOG = LoggerFactory.getLogger(TdiSdkJsonWebTokenComponent.class);

  /**
   * Constructor.
   *
   * @param componentName
   *          : Component name.
   * @param impl
   *          : {@link TdiImplementationShape} instance.
   */
  public TdiSdkJsonWebTokenComponent(String componentName, TdiImplementationShape impl) {
    super(componentName, impl);
  }

  /*
   * (non-Javadoc)
   * 
   * @see biz.neustar.tdi.fw.interfaces.TdiComponentShape#init()
   */
  @Override
  public CompletableFuture<Void> init() {
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Packs the completed claims in preparation for signing.
   * 
   * @param message
   *          : The TdiCanonicalMessage containing the claims.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> packClaims(TdiCanonicalMessageShape message) {
    CompletableFuture<TdiCanonicalMessageShape> result = new CompletableFuture<>();
    String jsonClaims = null;

    try {
      jsonClaims = Utils.objectToJson(message.getClaims());
      ((TdiCanonicalMessage) message)
          .setRawPayload(this.getPlatform().getUtils().b64UrlEncode(jsonClaims));
      result.complete(message);
    } catch (InvalidFormatException e) {
      result.completeExceptionally(e);
    }
    return result;
  }

  /**
   * Unpacks the claims from a verified payload.
   * 
   * @param message
   *          : The TdiCanonicalMessage containing the serialized claims.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> unpackClaims(
      TdiCanonicalMessageShape message) {
    CompletableFuture<TdiCanonicalMessageShape> result = new CompletableFuture<>();
    try {
      String jsonClaims = this.getPlatform().getUtils().b64UrlDecode(message.getRawPayload());

      TdiClaims claims = Utils.jsonToObject(jsonClaims, TdiClaims.class);
      message.setClaims(claims);

      result.complete(message);
    } catch (InvalidFormatException exp) {
      LOG.error("Failed in parsing JSON");
      result.completeExceptionally(exp);
    }
    return result;
  }
}
