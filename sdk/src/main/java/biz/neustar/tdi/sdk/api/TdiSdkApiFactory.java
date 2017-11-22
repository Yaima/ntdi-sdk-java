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

import biz.neustar.tdi.fw.implementation.TdiFlowArguments;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.sdk.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds the api flow arguments for the
 * {@link TdiImplementationShape#buildApiFlow(TdiFlowArguments, TdiFlowArguments)}
 * method.
 *
 */
public class TdiSdkApiFactory {
  /**
   * Returns the api flow arguments in the desired manner.
   *
   * @param imp
   *          : {@link TdiImplementationShape} instance
   *
   * @return map with 'key' as Api name and 'value' as {@link TdiFlowArguments}
   */
  public static Map<String, TdiFlowArguments> buildArgs(TdiImplementationShape imp) {
    Map<String, TdiFlowArguments> flows = new HashMap<>();
    addSignArgs(imp, flows);
    addCoSignArgs(imp, flows);
    addVerifyArgs(imp, flows);
    addVerifyGeneralArgs(imp, flows);
    return flows;
  }

  /**
   * Adds Sign api flows.
   *
   * @param imp
   *          : {@link TdiImplementationShape} instance
   *
   * @param flows
   *          map to set, key as Api name and value {@link TdiFlowArguments}
   */
  private static void addSignArgs(
      TdiImplementationShape imp,
      Map<String, TdiFlowArguments> flows) {
    Sign signFlow = new Sign(imp);
    TdiFlowArguments signArgs = new TdiFlowArguments();

    /** Create Canonical message and populate payload claim. */
    signArgs.addMethod(Constants.FlowMethods.handleInit, signFlow::handleInit);
    /** Optional secondary parsing step for inbound messages. */
    signArgs.addMethod(Constants.FlowMethods.Signing.parseRaw, signFlow::parseRaw);
    /** Loads values in to canonical "claims" key value pairs. */
    signArgs.addMethod(Constants.FlowMethods.Signing.setClaims, signFlow::setClaims);
    /** Populates canonical "rawPayload" field with serialized claims. */
    signArgs.addMethod(Constants.FlowMethods.Signing.packClaims, signFlow::packClaims);
    /** Assign "kid" strings to canonical "signers" array. */
    signArgs.addMethod(Constants.FlowMethods.Signing.setSigners, signFlow::setSigners);
    /** Signs the "rawPayload" with the keys from "signers". */
    signArgs.addMethod(Constants.FlowMethods.Signing.sign, signFlow::sign);
    /** Handles the canonical "builtMessage" field for final return. */
    signArgs.addMethod(Constants.FlowMethods.handleReturn, signFlow::handleReturn);

    flows.put(Constants.Api.SignFlow, signArgs);
  }

  /**
   * Adds CoSign api flows.
   *
   * @param imp
   *          : {@link TdiImplementationShape} instance
   *
   * @param flows
   *          map to set, key as Api name and value {@link TdiFlowArguments}
   */
  private static void addCoSignArgs(
      TdiImplementationShape imp,
      Map<String, TdiFlowArguments> flows) {
    CoSign cosignFlow = new CoSign(imp);
    TdiFlowArguments cosignArgs = new TdiFlowArguments();

    /** Create Canonical message &amp; populate payload claim and signatures. */
    cosignArgs.addMethod(Constants.FlowMethods.handleInit, cosignFlow::handleInit);
    /** assign "kid" strings to canonical "signers" array. */
    cosignArgs.addMethod(Constants.FlowMethods.Signing.setSigners, cosignFlow::setSigners);
    /** Co-signs the message. */
    cosignArgs.addMethod(Constants.FlowMethods.Signing.sign, cosignFlow::sign);
    /** Update the built message with heldSignatures. */
    cosignArgs.addMethod(Constants.FlowMethods.handleReturn, cosignFlow::handleReturn);

    flows.put(Constants.Api.CosignFlow, cosignArgs);
  }

  /**
   * Adds Verify api flows.
   *
   * @param imp
   *          : {@link TdiImplementationShape} instance
   *
   * @param flows
   *          map to set, key as Api name and value {@link TdiFlowArguments}
   */
  private static void addVerifyArgs(
      TdiImplementationShape imp,
      Map<String, TdiFlowArguments> flows) {
    Verify verifyFlow = new Verify(imp);
    TdiFlowArguments verifyArgs = new TdiFlowArguments();

    /** Create Canonical message and populate the "receivedMessage" field. */
    verifyArgs.addMethod(Constants.FlowMethods.handleInit, verifyFlow::handleInit);
    /** Break "receivedMessage" into its components (header/payload/sigs). */
    verifyArgs.addMethod(Constants.FlowMethods.Verifying.unpackEnvelope, verifyFlow::unpackEnvelope);
    /** Parse "rawClaims" in to "claims" key/values. */
    verifyArgs.addMethod(Constants.FlowMethods.Verifying.unpackClaims, verifyFlow::unpackClaims);
    /** Validate all "claims" properties. */
    verifyArgs.addMethod(Constants.FlowMethods.Verifying.validateClaims, verifyFlow::validateClaims);
    /** Manipulate and/or validate "signaturesToVerify". */
    verifyArgs.addMethod(Constants.FlowMethods.Verifying.prepSignatures, verifyFlow::prepSignatures);
    /** Validate signatures to verify relative to "rawPayload". */
    verifyArgs.addMethod(Constants.FlowMethods.Verifying.verifySignatures, verifyFlow::verifySignatures);
    /** Run any necessary cleanup or post-validation steps. */
    verifyArgs.addMethod(Constants.FlowMethods.Verifying.afterVerify, verifyFlow::afterVerify);
    /** Returns the authenticated message. */
    verifyArgs.addMethod(Constants.FlowMethods.handleReturn, verifyFlow::handleReturn);

    flows.put(Constants.Api.VerifyFlow, verifyArgs);
  }

  /**
   * Adds VerifyGeneral api flows.
   *
   * @param imp
   *          : {@link TdiImplementationShape} instance
   *
   * @param flows
   *          map to set, key as Api name and value {@link TdiFlowArguments}
   */
  private static void addVerifyGeneralArgs(
      TdiImplementationShape imp,
      Map<String, TdiFlowArguments> flows) {
    VerifyGeneral verifyGeneralFlow = new VerifyGeneral(imp);
    TdiFlowArguments verifyGeneralArgs = new TdiFlowArguments();
    /** Create Canonical message and populate the "receivedMessage" field. */
    verifyGeneralArgs.addMethod(Constants.FlowMethods.handleInit, verifyGeneralFlow::handleInit);
    /** Break "receivedMessage" into its components (header/payload/sigs). */
    verifyGeneralArgs.addMethod(Constants.FlowMethods.Verifying.unpackEnvelope, verifyGeneralFlow::unpackEnvelope);
    /** Parse "rawClaims" in to "claims" key/values. */
    verifyGeneralArgs.addMethod(Constants.FlowMethods.Verifying.unpackClaims, verifyGeneralFlow::unpackClaims);
    /** Validate all "claims" properties. */
    verifyGeneralArgs.addMethod(Constants.FlowMethods.Verifying.validateClaims, verifyGeneralFlow::validateClaims);
    /** Manipulate and/or validate "signaturesToVerify". */
    verifyGeneralArgs.addMethod(Constants.FlowMethods.Verifying.prepSignatures, verifyGeneralFlow::prepSignatures);
    /** Validate signatures to verify relative to "rawPayload". */
    verifyGeneralArgs.addMethod(Constants.FlowMethods.Verifying.verifySignatures, verifyGeneralFlow::verifySignatures);
    /** Run any necessary cleanup or post-validation steps. */
    verifyGeneralArgs.addMethod(Constants.FlowMethods.Verifying.afterVerify, verifyGeneralFlow::afterVerify);
    /** Returns the authenticated message. */
    verifyGeneralArgs.addMethod(Constants.FlowMethods.handleReturn, verifyGeneralFlow::handleReturn);

    flows.put(Constants.Api.VerifyGeneralFlow, verifyGeneralArgs);
  }
}
