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

package biz.neustar.tdi.sdk;

import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.platform.TdiPlatformBase;
import biz.neustar.tdi.fw.platform.TdiPlatformShapeFactory;
import biz.neustar.tdi.fw.plugin.TdiPluginBaseFactory;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;

import java.util.List;
import java.util.Map;

/**
 * SDK Options object to be filled and passed as argument to {@link TdiSdk} for
 * initialization.
 */
public class TdiSdkOptions {
  /**
   * Constructor/Method reference of the class extending {@link TdiPlatformBase}
   * to be initialized.
   */
  TdiPlatformShapeFactory platform;

  /**
   * Map of System configurations.
   */
  Map<String, Object> config;

  /**
   * List of Constructor/Method reference of the class extending
   * {@link TdiPluginBaseFactory}.
   */
  List<TdiPluginBaseFactory> plugins;

  /**
   * Flag option to expose the {@link TdiImplementationShape} via
   * {@link TdiSdkWrapperShape} if set to true.
   */
  boolean exposeImpl = false;
}
