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

package biz.neustar.tdi.fw.wrapper;

import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.plugin.TdiPluginBase;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Class implementing {@link TdiSdkWrapperShape}. The main object containing all the
 * APIs built through
 * {@link TdiImplementationShape#buildApiFlow(TdiFlowArguments, TdiFlowArguments)}
 * and all the plugins initialized through
 * {@link TdiImplementationShape#loadPlugins(TdiSdkWrapperShape, List)}
 */
public class TdiSdkWrapper implements TdiSdkWrapperShape {

  Map<String, Object> apis = new LinkedHashMap<>();
  Map<String, TdiPluginBase> plugins = new LinkedHashMap<>();
  TdiImplementationShape impl = null;

  @SuppressWarnings("unchecked")
  @Override
  public <T, R> Function<T, CompletableFuture<R>> api(String apiName) {
    return (Function<T, CompletableFuture<R>>) apis.get(apiName);
  }

  @Override
  public <T, R> void api(String apiName, Function<T, CompletableFuture<R>> api) {
    if (!StringUtils.isEmpty(apiName) && api != null) {
      apis.put(apiName, api);
    }
  }
  
  @Override
  public TdiPluginBase plugin(String pluginName) {
    return plugins.get(pluginName);
  }

  @Override
  public void plugin(TdiPluginBase plugin) {
    if (plugin != null) {
      plugins.put(plugin.getName(), plugin);
    }
  }

  @Override
  public Map<String, TdiPluginBase> plugins() {
    Map<String, TdiPluginBase> returnMap = new HashMap<>();
    returnMap.putAll(plugins);
    return returnMap;
  }

  @Override
  public TdiImplementationShape getImpl() {
    return this.impl;
  }

  @Override
  public void setImpl(TdiImplementationShape impl) {
    this.impl = impl;
  }
}
