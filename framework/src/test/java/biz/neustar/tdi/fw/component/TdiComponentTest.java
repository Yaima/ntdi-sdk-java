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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import biz.neustar.tdi.fw.TestData;
import biz.neustar.tdi.fw.implementation.TdiImplementation;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class TdiComponentTest {
  static TdiComponentShape component;

  @BeforeClass
  public static void setup() {
    component = (TdiComponentShape) getNewInstance(TestData.ComponentImpl::new);
  }

  /**
   * Method to return instance.
   * 
   * @param componentRef
   *          : Function reference.
   * 
   * @return : Instance.
   */
  public static TdiComponentShapeFactory getNewInstance(TdiComponentShapeFactory componentRef) {
    TdiImplementationShape impl = new TdiImplementation(new HashMap<>(),
        TestData.DummyPlatform::new);

    return componentRef.newInstance("dummy", impl);
  }

  @Test
  public void testGetters() {
    assertNotNull(component.getConfig());
    assertNotNull(component.getPlatform());
    assertEquals("dummy", component.getName());
    assertNotNull(((TdiComponent) component).getDataStore());
  }

  @Test
  public void testComponentInit() throws InterruptedException, ExecutionException {
    component.init().thenApply((arg) -> {
      return arg;
    }).thenAccept((arg) -> {
    }).get();

    assertTrue(true);
  }
}
