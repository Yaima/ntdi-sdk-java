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

package biz.neustar.tdi.fw.plugin;

import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;

/**
 * Functional interface for {@link TdiPluginBase}. Any class extending
 * {@link TdiPluginBase} MUST HAVE a constructor with the exact argument type
 * list as this interface.
 */
@FunctionalInterface
public interface TdiPluginBaseFactory {
  /**
   * Method specifying argument list that the extending class should have in one
   * of the constructors.
   * 
   * @param impl
   *          : {@link TdiImplementationShape} instance to be initialized with.
   * @param sdkWrapper
   *          : {@link TdiSdkWrapperShape} instance to be initialized with.
   * 
   * @return Instance of the class implementing this interface.
   * 
   */
  public TdiPluginBaseFactory newInstance(TdiImplementationShape impl,
      TdiSdkWrapperShape sdkWrapper);
}
