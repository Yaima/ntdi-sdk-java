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

package biz.neustar.tdi.fw.implementation;

import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessage;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class TdiImplementationTestData {

  /**
   * Creates a {@link CompletableFuture} as a step of original flow.
   * 
   * @param data
   *          the data to be acted upon
   * 
   * @return {@link CompletableFuture} for the step
   */
  public static CompletableFuture<? extends Object> handleFirst(Object data) {
    return CompletableFuture.supplyAsync(() -> {
      TdiCanonicalMessage msgObj = new TdiCanonicalMessage(1);
      msgObj.setRawPayload(((String) data) + "#FirstStep");
      return msgObj;
    });
  }

  /**
   * Creates the original flow arguments.
   * 
   * @return {@link TdiFlowArguments} instance with original flow arguments
   */
  public static TdiFlowArguments getOriginalFlow() {

    TdiFlowArguments originalFlow = new TdiFlowArguments();

    originalFlow.addMethod("First", TdiImplementationTestData::handleFirst);

    originalFlow.addMethod("Second", (data) -> {
      TdiCanonicalMessage msgObj = (TdiCanonicalMessage) data;
      msgObj.setRawPayload(msgObj.getRawPayload() + "#SecondStep");
      return CompletableFuture.completedFuture(msgObj);
    });

    originalFlow.addMethod("Third", (data) -> {
      return CompletableFuture.supplyAsync(() -> {
        TdiCanonicalMessage msgObj = (TdiCanonicalMessage) data;
        msgObj.setRawPayload(msgObj.getRawPayload() + "#ThirdStep");
        return msgObj;
      });
    });
    originalFlow.addMethod("Final", (data) -> {
      TdiCanonicalMessage msgObj = (TdiCanonicalMessage) data;
      msgObj.setRawPayload(msgObj.getRawPayload() + "#FourthStep");
      return CompletableFuture.completedFuture(msgObj.getRawPayload());
    });

    return originalFlow;
  }

  /**
   * Creates the overriding and additional flow arguments.
   * 
   * @return {@link TdiFlowArguments} instance with overriding and additional
   *         flow arguments
   */
  public static TdiFlowArguments getOtherFlow() {

    TdiFlowArguments otherFlow = new TdiFlowArguments();

    otherFlow.addOverrideSteps(Arrays.asList("Third"));

    otherFlow.addMethod("Second", (data) -> {
      TdiCanonicalMessage msgObj = (TdiCanonicalMessage) data;
      msgObj.setRawPayload(msgObj.getRawPayload() + "#AdditionalSecondStep");
      return CompletableFuture.completedFuture(msgObj);
    });

    otherFlow.addMethod("Third", (data) -> {

      return CompletableFuture.supplyAsync(() -> {
        TdiCanonicalMessage msgObj = (TdiCanonicalMessage) data;
        msgObj.setRawPayload(msgObj.getRawPayload() + "#OverridingThirdStep");
        return msgObj;
      });
    });

    return otherFlow;
  }

  /**
   * Creates the overriding and additional flow arguments.
   * 
   * @return {@link TdiFlowArguments} instance with overriding and additional
   *         flow arguments
   */
  public static TdiFlowArguments getOtherFlow1() {

    TdiFlowArguments otherFlow = getOtherFlow();
    otherFlow.addOverrideStep("Third");
    return otherFlow;
  }

}
