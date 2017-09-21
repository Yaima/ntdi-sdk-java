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

package biz.neustar.tdi.fw.platform;

import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;

import java.util.Map;
import java.util.Set;

/**
 * Abstract parent of the actual {@link TdiPlatformShape} implementation.
 */
public abstract class TdiPlatformBase implements TdiPlatformShape {
  /**
   * Platform configurations.
   */
  protected Map<String, Object> config;

  /**
   * Default constructor initialized with default configurations.
   * 
   * @param config
   *          : Config to be initialized with.
   */
  public TdiPlatformBase(Map<String, Object> config) {
    this.config = config;
  }

  @Override
  public Map<String, Object> getConfig() {
    return config;
  }

  @Override
  public boolean checkConfig(Set<String> keys) {
    Set<String> configKeys = this.getConfig().keySet();

    if (configKeys.containsAll(keys) == false) {
      keys.removeAll(configKeys);
      throw new FrameworkRuntimeException("Missing required config settings: " + keys);
    }

    return true;
  }

  /**
   * Dummy implementation to avoid the classes extending {@link TdiPlatformBase}
   * to override or write this in their code. <br>
   * {@inheritDoc}
   */
  @Override
  public TdiPlatformShapeFactory newInstance(Map<String, Object> conf) {
    return null;
  }
}
