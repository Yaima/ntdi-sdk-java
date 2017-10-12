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

import static org.junit.Assert.assertEquals;

import biz.neustar.tdi.fw.TestData;
import biz.neustar.tdi.fw.implementation.TdiFlowArguments;
import biz.neustar.tdi.fw.implementation.TdiImplementation;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.plugin.TdiPluginBase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class TdiSdkWrapperTest {
  static TdiSdkWrapperShape sdkWrapper;

  @BeforeClass
  public static void setup() {
    sdkWrapper = new TdiSdkWrapper();
  }

  @Test
  public void testDefaultFlowsGettersAndSetters() {
    Map<String, TdiFlowArguments> defaultFlow = new HashMap<>();
    
    sdkWrapper.setDefaultFlows(defaultFlow);
    assertEquals(defaultFlow, sdkWrapper.getDefaultFlows());
  }
  
  @Test
  public void testApisGettersAndSetters() {
    Function<Object, CompletableFuture<Object>> func = 
        new Function<Object, CompletableFuture<Object>>() {

      @Override
      public CompletableFuture<Object> apply(Object input) {
        return null;
      }
    };

    sdkWrapper.api("apiName", func);
    assertEquals(func, sdkWrapper.api("apiName"));
  }

  @Test
  public void testApiGettersAndSettersWithNullArguments() {
    Function<Object, CompletableFuture<Object>> func = 
        new Function<Object, CompletableFuture<Object>>() {

      @Override
      public CompletableFuture<Object> apply(Object input) {
        return null;
      }
    };
    
    sdkWrapper.api(null, func);
    sdkWrapper.api("apiName2", null);
    sdkWrapper.api(null, null);
  }

  @Test
  public void testPluginsGettersAndSetters() {
    TdiImplementationShape impl = new TdiImplementation(new HashMap<>(),
        TestData.DummyPlatform::new);
    TdiPluginBase plugin = new TdiPluginBase("pluginName", impl, sdkWrapper) {

      @Override
      public CompletableFuture<Boolean> init() {
        return null;
      }
    };

    sdkWrapper.plugin(plugin);
    assertEquals(plugin, sdkWrapper.plugin("pluginName"));
    assertEquals(1, sdkWrapper.plugins().size());

    sdkWrapper.setImpl(impl);
    assertEquals(impl, sdkWrapper.getImpl());
  }

  @Test
  public void testPluginsGettersAndSettersWithNullArguments() {
    // Test that with null plugin as argument nothing is changed.
    int pluginsSize = sdkWrapper.plugins().size();
    sdkWrapper.plugin((TdiPluginBase) null);
    assertEquals(pluginsSize, sdkWrapper.plugins().size());

    // Test that with null sdkWrapper name and/or null sdkWrapper nothing is
    // changed.
    pluginsSize = sdkWrapper.plugins().size();
    TdiPluginBase nullPlugin = null;

    sdkWrapper.plugin(nullPlugin);
    assertEquals(pluginsSize, sdkWrapper.plugins().size());
  }
}
