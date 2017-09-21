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

import static org.junit.Assert.assertNotNull;

import biz.neustar.tdi.fw.TestData;
import biz.neustar.tdi.fw.implementation.TdiImplementationShape;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TdiClassFactoryTest {
  @Test
  public void testLibrary() throws Exception {
    Map<String, Object> config = new HashMap<>();

    TdiClassFactoryShape library = new TdiClassFactory(config);
    CompletableFuture<TdiImplementationShape> future = library.create(TestData.DummyPlatform::new);
    future.thenAccept((impl) -> {
      assertNotNull(impl);
    });
    future.get();
  }
}
