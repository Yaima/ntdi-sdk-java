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
import biz.neustar.tdi.fw.component.TdiComponent;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import biz.neustar.tdi.fw.exception.InvalidFormatException;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.utils.Utils;
import biz.neustar.tdi.sdk.Constants.DefaultJws;
import biz.neustar.tdi.sdk.component.jws.TdiJws;
import biz.neustar.tdi.sdk.component.jws.TdiJwsHeader;
import biz.neustar.tdi.sdk.component.jws.TdiJwsSignature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * This component is the parser/packer layer for messages with the JWS wire
 * format.
 */
public class TdiSdkJsonWebSignature extends TdiComponent {
  private static final Logger LOG = LoggerFactory.getLogger(TdiSdkJsonWebSignature.class);
  private static String jwsPatternExpr = "^[a-zA-Z0-9-_]+\\.[a-zA-Z0-9-_]+\\.[a-zA-Z0-9-_]+$";

  /**
   * Constructor.
   * 
   * @param componentName
   *          : Component name.
   * @param impl
   *          : {@link TdiImplementationShape} instance.
   */
  public TdiSdkJsonWebSignature(String componentName, TdiImplementationShape impl) {
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
   * Calls the cryptography layer to sign a serialized payload and places the
   * resulting signatures in the JWS envelope.
   * 
   * @param message
   *          : The {@link TdiCanonicalMessage} containing the payload to
   *          verify.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> sign(TdiCanonicalMessageShape message) {
    try {
      TdiJws jwsMessage = new TdiJws();
      List<CompletableFuture<?>> queue = new ArrayList<>();

      jwsMessage.payload = message.getRawPayload();

      for (TdiKeyStructureShape keyStruct : message.getSigners()) {
        TdiJwsHeader header = new TdiJwsHeader();
        header.typ = !StringUtils.isEmpty(message.getSignatureType()) ? message.getSignatureType()
            : DefaultJws.type;
        header.alg = DefaultJws.alg;
        header.kid = keyStruct.getKeyId();

        TdiJwsSignature signature = new TdiJwsSignature();

        signature.parsedHeader = header;
        signature.protectedHeader = this.getPlatform().getUtils()
            .b64UrlEncode(Utils.objectToJson(header));

        String toSign = signature.protectedHeader.concat(".").concat(message.getRawPayload());

        queue.add(
            this.getPlatform().getCrypto().sign(keyStruct, toSign).thenApply((finalSignature) -> {
              signature.signature = finalSignature;
              jwsMessage.signatures.add(signature);

              return true;
            }));
      }

      return CompletableFuture.allOf(queue.toArray(new CompletableFuture<?>[0]))
          .thenCompose((arg) -> {
            CompletableFuture<TdiCanonicalMessageShape> result = new CompletableFuture<>();

            try {
              message.setBuiltMessage(Utils.objectToJson(jwsMessage));
              result.complete(message);
            } catch (InvalidFormatException e) {
              result.completeExceptionally(e);
            }
            return result;
          });
    } catch (InvalidFormatException exp) {
      CompletableFuture<TdiCanonicalMessageShape> result = new CompletableFuture<>();
      result.completeExceptionally(exp);
      return result;
    }
  }

  /**
   * Unpacks a protected payload from the JWS envelope.
   * 
   * @param message
   *          : The {@link TdiCanonicalMessage} containing the payload to
   *          deserialize.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   * 
   * @throws FrameworkRuntimeException
   *           if the message is either empty or is not in the required format.
   */
  public CompletableFuture<TdiCanonicalMessageShape> unpack(TdiCanonicalMessageShape message) {
    String receivedMessage = message.getReceivedMessage();
    CompletableFuture<TdiCanonicalMessageShape> result = new CompletableFuture<>();

    if (StringUtils.isEmpty(receivedMessage)) {
      LOG.error("Empty received message");
      throw new FrameworkRuntimeException("Empty received message");
    }

    try {
      if (Pattern.matches(jwsPatternExpr, receivedMessage)) {
        // Is a compact JWS message.
        String[] splits = receivedMessage.split("\\.");
        TdiJwsSignature signature = new TdiJwsSignature();

        signature.parsedHeader = Utils.jsonToObject(
            this.getPlatform().getUtils().b64UrlDecode(splits[0]), TdiJwsHeader.class);
        signature.protectedHeader = splits[0];
        signature.signature = splits[2];

        message.addSignatureToVerify(signature);
        message.setRawPayload(splits[1]);
      } else {
        TdiJws jwsMessage = Utils.jsonToObject(receivedMessage, TdiJws.class);
        if (!StringUtils.isEmpty(jwsMessage.payload) && jwsMessage.signatures != null
            && !jwsMessage.signatures.isEmpty()) {
          // A normal JWS message.
          message.setRawPayload(jwsMessage.payload);
          for (TdiJwsSignature signature : jwsMessage.signatures) {
            String headerJson = this.getPlatform().getUtils()
                .b64UrlDecode(signature.protectedHeader);
            signature.parsedHeader = Utils.jsonToObject(headerJson, TdiJwsHeader.class);
            message.addSignatureToVerify(signature);
          }
        } else {
          LOG.error("Invalid token format");
          result.completeExceptionally(new FrameworkRuntimeException("Invalid token format"));
        }
      }
      result.complete(message);
    } catch (InvalidFormatException exp) {
      result.completeExceptionally(exp);
    }
    return result;
  }

  /**
   * Calls the cryptography layer to verify that the JWS is authentic.
   * 
   * @param message
   *          : The TdiCanonicalMessage containing the payload to verify.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: {@link TdiCanonicalMessageShape}. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<TdiCanonicalMessageShape> verify(TdiCanonicalMessageShape message) {
    List<CompletableFuture<?>> queue = new ArrayList<>();
    for (int loopIndex = 0; loopIndex < message.getSignaturesToVerify().size(); loopIndex++) {
      TdiJwsSignature signature = (TdiJwsSignature) message.getSignaturesToVerify().get(loopIndex);
      queue.add(this.getPlatform().getKeystore().getKey(signature.parsedHeader.kid)
          .thenCompose((TdiKeyStructureShape key) -> {
            String toVerifyPayload = (signature.protectedHeader).concat(".")
                .concat(message.getRawPayload());
            return this.getPlatform().getCrypto().verify(key, toVerifyPayload, signature.signature);
          }));
    }
    return CompletableFuture.allOf(queue.toArray(new CompletableFuture<?>[0])).thenApply((arg) -> {
      return message;
    });
  }
}
