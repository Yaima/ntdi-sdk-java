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

import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.platform.TdiPlatformShape;

import java.util.Map;

/**
 * Base implementation for {@link TdiComponentShape} representing fundamental
 * operations that are performed on messages.
 */
public abstract class TdiComponent implements TdiComponentShape {
  /**
   * Configurations associated with component.
   */
  Map<String, Object> config;

  /**
   * Platform instance to access the datastore.
   */
  TdiPlatformShape platform;

  /**
   * Delegate delegate associated with the component.
   */
  DatastoreDelegate dataStore;

  /**
   * Component Name.
   */
  String componentName;

  /**
   * Default constructor
   * 
   * @param componentName
   *          : component name
   * 
   * @param impl
   *          : {@link TdiImplementationShape} instance to be initialized with.
   */
  public TdiComponent(String componentName, TdiImplementationShape impl) {
    this.componentName = componentName;
    this.platform = impl.getPlatform();
    this.config = impl.getConfig();
    this.dataStore = new DatastoreDelegate(this.getName(), this.platform);
  }

  @Override
  public TdiPlatformShape getPlatform() {
    return platform;
  }

  @Override
  public Map<String, Object> getConfig() {
    return config;
  }

  /*
   * (non-Javadoc)
   * 
   * @see biz.neustar.tdi.fw.interfaces.TdiComponentShape#getName()
   */
  @Override
  public String getName() {
    return componentName;
  }

  /**
   * Method to return the delegate instance of the datastore.
   * 
   * @return {@link DatastoreDelegate}
   */
  public DatastoreDelegate getDataStore() {
    return this.dataStore;
  }

  /**
   * Dummy implementation to avoid classes extending {@link TdiComponent} to
   * override/write this method in them. <br>
   * {@inheritDoc}
   */
  @Override
  public TdiComponentShapeFactory newInstance(String componentName, TdiImplementationShape impl) {
    return null;
  }
}
