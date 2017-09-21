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

package biz.neustar.tdi.fw.component;

import biz.neustar.tdi.fw.platform.TdiPlatformShape;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface representing fundamental operations that are performed on messages.
 */
public interface TdiComponentShape extends TdiComponentShapeFactory {
  /**
   * Returns the name of the component.
   * 
   * @return String
   */
  public String getName();

  /**
   * Returns the instance of {@link TdiPlatformShape}.
   * 
   * @return {@link TdiPlatformShape}
   */
  public TdiPlatformShape getPlatform();

  /**
   * Returns the configurations map associated with this component.
   * 
   * @return Map&lt;String, Object&gt;
   */
  public Map<String, Object> getConfig();

  /**
   * Method to perform initialization task and complete the
   * {@link CompletableFuture} upon successful initialization.
   * 
   * @return {@link CompletableFuture}
   */
  public CompletableFuture<Void> init();
}
