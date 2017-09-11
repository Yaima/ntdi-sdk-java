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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class TdiFlowArguments {

  /**
   * Contains sequential steps in case of original flow and
   * additional/overriding steps in other flow.
   */
  Map<String, Function<Object, CompletableFuture<? extends Object>>> flowMap;

  /**
   * Override steps list.
   */
  List<String> overrideSteps;

  /**
   * Constructor.
   */
  public TdiFlowArguments() {

    flowMap = new LinkedHashMap<>();
    overrideSteps = new ArrayList<>();
  }

  /**
   * Adds the override step.
   * 
   * @param step
   *          : the step to override
   */
  public void addOverrideStep(String step) {
    overrideSteps.add(step);
  }

  /**
   * Adds the override steps.
   * 
   * @param steps
   *          : list of the steps to override
   */
  public void addOverrideSteps(List<String> steps) {
    overrideSteps.addAll(steps);
  }

  /**
   * Adds the steps and their method references. Note: In case of original flow,
   * add the steps as per desired sequence.
   * 
   * @param step
   *          the step name
   * @param func
   *          the method reference
   */
  public void addMethod(String step, Function<Object, CompletableFuture<? extends Object>> func) {
    flowMap.put(step, func);
  }

  public Map<String, Function<Object, CompletableFuture<? extends Object>>> getFlowMap() {
    return flowMap;
  }

  public List<String> getOverrideSteps() {
    return overrideSteps;
  }

}
