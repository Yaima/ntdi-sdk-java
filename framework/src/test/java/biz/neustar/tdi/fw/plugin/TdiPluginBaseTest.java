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

import static org.junit.Assert.assertNotNull;

import biz.neustar.tdi.fw.TestData;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import biz.neustar.tdi.fw.implementation.TdiImplementation;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapper;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class TdiPluginBaseTest {
  static TdiSdkWrapperShape sdkWrapper;
  static TdiImplementationShape impl;
  static TdiPluginBase goodPlugin;
  
  /**
   * Test data setup.
   *  
   * @throws Exception if an error occurs
   */
  @BeforeClass
  public static void setup() throws Exception {
    sdkWrapper = new TdiSdkWrapper();
    impl = new TdiImplementation(new HashMap<>(), TestData.DummyPlatform::new);
    goodPlugin = new TdiPluginBase(impl, sdkWrapper) {

      @Override
      public CompletableFuture<Boolean> init() {
        return null;
      }

      @Override
      public String getName() {
        return "plugin";
      }
    };
  }

  @Test
  public void testInstanceCreation() throws Exception {
    new TdiPluginBase(impl, sdkWrapper) {

      @Override
      public CompletableFuture<Boolean> init() {
        return null;
      }

      @Override
      public String getName() {
        return "plugin";
      }
    };
  }

  @Test(expected = FrameworkRuntimeException.class)
  public void testInstanceExceptionOnNoName() throws Exception {
    new TdiPluginBase(impl, sdkWrapper) {

      @Override
      public CompletableFuture<Boolean> init() {
        return null;
      }

      @Override
      public String getName() {
        return null;
      }
    };
  }

  @Test(expected = FrameworkRuntimeException.class)
  public void testInstanceExceptionOnNoImpl() throws Exception {
    new TdiPluginBase(null, sdkWrapper) {

      @Override
      public CompletableFuture<Boolean> init() {
        return null;
      }

      @Override
      public String getName() {
        return "plugin";
      }
    };
  }

  @Test(expected = FrameworkRuntimeException.class)
  public void testInstanceExceptionOnNoApi() throws Exception {
    new TdiPluginBase(impl, null) {

      @Override
      public CompletableFuture<Boolean> init() {
        return null;
      }

      @Override
      public String getName() {
        return "plugin";
      }
    };
  }

  @Test
  public void testGetters() {
    assertNotNull(goodPlugin.getSdkWrapper());
    assertNotNull(goodPlugin.getDataStore());
    assertNotNull(goodPlugin.getName());
  }

  @Test
  public void testValidateStoreAccess() {
    assertNotNull(goodPlugin.validatePluginDataStore(Arrays.asList("key1", "key2")));
  }
}
