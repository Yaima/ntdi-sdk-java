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

package biz.neustar.tdi.fw.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import biz.neustar.tdi.fw.TestData;
import biz.neustar.tdi.fw.canonicalmessage.TdiCanonicalMessageShape;
import biz.neustar.tdi.fw.component.TdiComponentShapeFactory;
import biz.neustar.tdi.fw.implementation.TdiImplementation;
import biz.neustar.tdi.fw.keystructure.TdiKeyStructureShape;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapper;
import biz.neustar.tdi.fw.wrapper.TdiSdkWrapperShape;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Test class for {@link TdiImplementation}.
 * 
 *
 */
public class TdiImplementationTest {
  private static TdiImplementation impl;
  private static TdiImplementation impl1;
  private static Map<String, Object> config;

  /**
   * Setup.
   * 
   */
  @BeforeClass
  public static void setup() {
    config = new HashMap<>();
    config.put("key1", "value1");
    config.put("key2", "value2");
    config.put("key3", "value3");

    impl = new TdiImplementation(config, TestData.DummyPlatform::new);
    impl1 = new TdiImplementation(config, TestData.DummyPlatform1::new);
  }

  @Test
  public void testGetters() throws InterruptedException, ExecutionException {
    CompletableFuture<TdiKeyStructureShape> future = impl.getSelfKey();
    assertEquals(TestData.goodKey, future.get());

    assertNotNull(impl.getPlatform());
    assertEquals(config, impl.getConfig());
  }

  @Test
  public void testModule() {
    impl.loadModule("Module1", TestData.ComponentImpl::new);
    assertNotNull(impl.getModule("Module1"));
  }

  @Test
  public void testModules() {
    Map<String, TdiComponentShapeFactory> modules = new HashMap<>();
    modules.put("Module2", TestData.ComponentImpl::new);
    modules.put("Module3", TestData.ComponentImpl::new);
    modules.put("Module4", TestData.ComponentImpl::new);

    impl.loadModules(modules);

    assertNotNull(impl.getModule("Module2"));
    assertNotNull(impl.getModule("Module3"));
    assertNotNull(impl.getModule("Module4"));
  }

  @Test
  public void testGenerateMsg() throws InterruptedException, ExecutionException {
    CompletableFuture<TdiCanonicalMessageShape> future = impl.generateMsg(null);
    TdiCanonicalMessageShape msg = future.get();
    assertNotNull(msg);
    assertEquals("Fleet1", msg.getCurrentProject());

    future = impl.generateMsg("fleetId");
    msg = future.get();
    assertNotNull(msg);
    assertEquals("fleetId", msg.getCurrentProject());
  }
  
  @Test(expected = ExecutionException.class)
  public void testGenerateMsg_Exception() throws InterruptedException, ExecutionException {

    CompletableFuture<TdiCanonicalMessageShape> future = impl1.generateMsg(null);
    future.get();
  }

  @Test
  public void testBuildApiFlowOnlyOriginalFlow() throws Exception {

    Function<String, CompletableFuture<String>> api = impl
        .buildApiFlow(TdiImplementationTestData.getOriginalFlow(), null);

    CompletableFuture<String> returnedFuture = api.apply("Hello");

    assertEquals("Hello#FirstStep#SecondStep#ThirdStep#FourthStep", returnedFuture.get());
  }

  @Test
  public void testBuildApiFlowWithOtherFlows() throws Exception {

    Function<String, CompletableFuture<String>> api = impl
        .buildApiFlow(TdiImplementationTestData.getOriginalFlow(),
            TdiImplementationTestData.getOtherFlow());

    CompletableFuture<String> returnedFuture = api.apply("Hello");

    assertEquals("Hello#FirstStep#SecondStep#AdditionalSecondStep#OverridingThirdStep#FourthStep",
        returnedFuture.get());
  }
  
  @Test
  public void testBuildApiFlowWithOtherFlows1() throws Exception {

    Function<String, CompletableFuture<String>> api = impl
        .buildApiFlow(TdiImplementationTestData.getOriginalFlow(),
            TdiImplementationTestData.getOtherFlow1());

    CompletableFuture<String> returnedFuture = api.apply("Hello");

    assertEquals("Hello#FirstStep#SecondStep#AdditionalSecondStep#OverridingThirdStep#FourthStep",
        returnedFuture.get());
  }

  @Test
  public void testValidateDataStore_WithCheckPresentInDatastore() throws Exception {

    CompletableFuture<?> result = impl.validateDataStore("testStore", Arrays.asList("key1"));

    assertEquals(Boolean.TRUE, result.get());
  }

  @Test
  public void testValidateDataStore_WithCheckNotPresentInDatastore() throws Exception {

    CompletableFuture<?> result = impl.validateDataStore("testStore", Arrays.asList("key2"));

    assertEquals(Boolean.TRUE, result.get());
  }

  @Test
  public void testValidateDataStore_WithNonExistentCheck() throws Exception {

    CompletableFuture<?> result = impl.validateDataStore("testStore", Arrays.asList("key4"));

    assertEquals(Boolean.TRUE, result.get());
  }

  @Test
  public void testLoadPlugins() throws Exception {
    TdiSdkWrapperShape sdkWrapper = new TdiSdkWrapper();

    assertNull(sdkWrapper.plugin("PluginImpl"));
    CompletableFuture<TdiSdkWrapperShape> future = impl.loadPlugins(sdkWrapper,
        Arrays.asList(TestData.PluginImpl::new));
    future.get();
    assertNotNull(sdkWrapper.plugin("PluginImpl"));
  }

  @Test
  public void testLoadEmptyPluginsList() throws Exception {
    TdiSdkWrapperShape sdkWrapper = new TdiSdkWrapper();

    assertEquals(0, sdkWrapper.plugins().size());
    CompletableFuture<TdiSdkWrapperShape> future = impl.loadPlugins(sdkWrapper, Arrays.asList());
    future.get();
    assertEquals(0, sdkWrapper.plugins().size());
  }
  
  @Test
  public void testSetupTdiStore() throws Exception {
    CompletableFuture<Boolean> result =  impl.setupTdiStore("{}");
    assertEquals(Boolean.TRUE, result.get());
  }
  
  @Test
  public void testSetupTdiStoreWithException() throws Exception {
    CompletableFuture<Boolean> result =  impl.setupTdiStore("{");
    assertEquals(Boolean.TRUE, result.get());
  }
}
