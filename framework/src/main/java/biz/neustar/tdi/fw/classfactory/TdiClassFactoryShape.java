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

package biz.neustar.tdi.fw.classfactory;

import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.platform.TdiPlatformShapeFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for the TdiClassFactory.
 *
 */
public interface TdiClassFactoryShape {

  /**
   * Creates a new {@link TdiImplementationShape} instance and initializes the
   * platform. for it
   * 
   * @param pfFactory
   *          {@link TdiPlatformShapeFactory} instance
   * 
   * @return {@link CompletableFuture} of type {@link TdiImplementationShape}
   * 
   */
  public CompletableFuture<TdiImplementationShape> create(TdiPlatformShapeFactory pfFactory);

}
