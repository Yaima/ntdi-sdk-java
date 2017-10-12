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
  private static void addSignArgs(TdiImplementationShape imp, Map<String, TdiFlowArguments> flows) {

    Sign signFlow = new Sign(imp);
    TdiFlowArguments signArgs = new TdiFlowArguments();

    signArgs.addMethod(ApiFlows.SignFlow.handleInit.name(), signFlow::handleInit);
    signArgs.addMethod(ApiFlows.SignFlow.parseRaw.name(), signFlow::parseRaw);
    signArgs.addMethod(ApiFlows.SignFlow.setClaims.name(), signFlow::setClaims);
    signArgs.addMethod(ApiFlows.SignFlow.packClaims.name(), signFlow::packClaims);
    signArgs.addMethod(ApiFlows.SignFlow.setSigners.name(), signFlow::setSigners);
    signArgs.addMethod(ApiFlows.SignFlow.sign.name(), signFlow::sign);
    signArgs.addMethod(ApiFlows.SignFlow.handleReturn.name(), signFlow::handleReturn);

    flows.put(Constants.Api.SignFlow.name(), signArgs);
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
  private static void addCoSignArgs(TdiImplementationShape imp,
      Map<String, TdiFlowArguments> flows) {

    CoSign cosignFlow = new CoSign(imp);
    TdiFlowArguments cosignArgs = new TdiFlowArguments();

    cosignArgs.addMethod(ApiFlows.CosignFlow.handleInit.name(), cosignFlow::handleInit);
    cosignArgs.addMethod(ApiFlows.CosignFlow.setSigners.name(), cosignFlow::setSigners);
    cosignArgs.addMethod(ApiFlows.CosignFlow.sign.name(), cosignFlow::sign);
    cosignArgs.addMethod(ApiFlows.CosignFlow.handleReturn.name(), cosignFlow::handleReturn);

    flows.put(Constants.Api.CosignFlow.name(), cosignArgs);
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
  private static void addVerifyArgs(TdiImplementationShape imp,
      Map<String, TdiFlowArguments> flows) {

    Verify verifyFlow = new Verify(imp);
    TdiFlowArguments verifyArgs = new TdiFlowArguments();

    verifyArgs.addMethod(ApiFlows.VerifyFlow.handleInit.name(), verifyFlow::handleInit);
    verifyArgs.addMethod(ApiFlows.VerifyFlow.unpackEnvelope.name(), verifyFlow::unpackEnvelope);
    verifyArgs.addMethod(ApiFlows.VerifyFlow.unpackClaims.name(), verifyFlow::unpackClaims);
    verifyArgs.addMethod(ApiFlows.VerifyFlow.validateClaims.name(), verifyFlow::validateClaims);
    verifyArgs.addMethod(ApiFlows.VerifyFlow.prepSignatures.name(), verifyFlow::prepSignatures);
    verifyArgs.addMethod(ApiFlows.VerifyFlow.verifySignatures.name(), verifyFlow::verifySignatures);
    verifyArgs.addMethod(ApiFlows.VerifyFlow.afterVerify.name(), verifyFlow::afterVerify);
    verifyArgs.addMethod(ApiFlows.VerifyFlow.handleReturn.name(), verifyFlow::handleReturn);

    flows.put(Constants.Api.VerifyFlow.name(), verifyArgs);
  }

}
