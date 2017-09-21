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

import static org.junit.Assert.assertNotNull;

import biz.neustar.tdi.fw.TestData;
import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class DataStoreDelegateTest {
  static TdiPlatformShape platform;
  static Map<String, Object> config;

  @BeforeClass
  public static void setup() throws Exception {
    config = new HashMap<>();
    platform = new TestData.DummyPlatform(config);
  }

  @Test
  public void testGetters() {
    DatastoreDelegate delegate = new DatastoreDelegate("store", platform);

    assertNotNull(delegate.get("key1"));
    assertNotNull(delegate.set("key1", "value1"));
    assertNotNull(delegate.keys());
    assertNotNull(delegate.drop("key1"));
  }
}
