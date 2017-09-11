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

import biz.neustar.tdi.fw.component.TdiComponentShapeFactory;
import biz.neustar.tdi.sdk.Constants.Components;

import java.util.HashMap;
import java.util.Map;

/**
 * Utils for components.
 *
 */
public class ComponentUtils {

  /**
   * Creates a map of modules with key as Component names and value as method
   * references to create objects.
   * 
   * @return the created map
   */
  public static Map<String, TdiComponentShapeFactory> getModules() {
    Map<String, TdiComponentShapeFactory> modules = new HashMap<>();
    modules.put(Components.JWS, TdiSdkJsonWebSignature::new);
    modules.put(Components.JWT, TdiSdkJsonWebTokenComponent::new);
    modules.put(Components.EXP, TdiSdkExpiresComponent::new);
    modules.put(Components.NBF, TdiSdkNotBeforeComponent::new);
    modules.put(Components.NONCE, TdiSdkNonceComponent::new);
    return modules;
  }

}
