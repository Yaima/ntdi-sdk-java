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

import biz.neustar.tdi.fw.implementation.TdiImplementation;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.platform.TdiPlatformShapeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * TdiClassFactory class, acting as a Implementation Factory.
 *
 */
public class TdiClassFactory implements TdiClassFactoryShape {

  /**
   * Logger instance.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TdiClassFactory.class);

  /**
   * Configuration for datastore defaults.
   */
  Map<String, Object> config;

  /**
   * Constructor.
   * 
   * @param conf
   *          : Configuration to be initialized with.
   */
  public TdiClassFactory(Map<String, Object> conf) {
    config = conf;
  }

  @Override
  public CompletableFuture<TdiImplementationShape> create(TdiPlatformShapeFactory pfFactory) {

    LOG.debug("TDI FRAMEWORK");

    TdiImplementation impl = new TdiImplementation(config, pfFactory);

    CompletableFuture<Void> pfInit = impl.getPlatform().init();

    return pfInit.thenApply((arg) -> {
      return impl;
    });
  }

}
