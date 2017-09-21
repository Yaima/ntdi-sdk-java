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

package biz.neustar.tdi.sdk.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import biz.neustar.tdi.sdk.TestData;
import biz.neustar.tdi.sdk.component.TdiSdkNotBeforeComponent;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

/**
 * Unit test cases for {@link TdiSdkNotBeforeComponent}. 
 */
public class TdiSdkNotBeforeComponentTest {
  static TdiSdkNotBeforeComponent component = null;
  
  @BeforeClass
  public static void setup() {
    component = new TdiSdkNotBeforeComponent("nbf", TestData.getDummyImplementation());
  }
  
  @Test
  public void testCreate() {
    Long timestamp = component.create();
    assertTrue(timestamp <= System.currentTimeMillis() / 1000);
  }
  
  @Test
  public void testCheck() {
    Long timestamp = component.create();
    assertFalse(component.check(timestamp));
    timestamp = System.currentTimeMillis() / 1000 - 300;
    assertTrue(component.check(timestamp));
  }
  
  @Test
  public void testMiscellaneous() {
    assertEquals("nbf", component.getName());
    CompletableFuture<Void> future = component.init(); 
    assertNotNull(future);
    assertTrue(future.isDone());
  }
}
