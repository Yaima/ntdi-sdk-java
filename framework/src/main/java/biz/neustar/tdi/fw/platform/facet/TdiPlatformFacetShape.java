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

package biz.neustar.tdi.fw.platform.facet;

import biz.neustar.tdi.fw.platform.TdiPlatformShape;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for platform facet. Facets are commonly associated features of a
 * platform that compose to form our generalized platform interface.
 */
public interface TdiPlatformFacetShape {
  /**
   * Returns the platform instance.
   * 
   * @return {@link TdiPlatformShape}
   */
  public TdiPlatformShape getPlatform();

  /**
   * Returns an instance of {@link CompletableFuture} which would be completed
   * and asynchronously call the call back once the init execution is finished.
   * 
   * @return {@link CompletableFuture} with either of the following states: <br>
   *         <b>Completed Successfully</b>: Void. <br>
   *         <b>Completed Exceptionally</b>: {@link Exception} in case of failure.
   */
  public CompletableFuture<Void> init();
}
