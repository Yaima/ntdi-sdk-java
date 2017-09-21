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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import biz.neustar.tdi.fw.TestData;
import biz.neustar.tdi.fw.exception.FrameworkRuntimeException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TdiPlatformBaseTest {
  private static TdiPlatformShape libraryPlatform;
  static Map<String, Object> config;

  @BeforeClass
  public static void setup() {
    config = new HashMap<>();
    libraryPlatform = new TestData.DummyPlatformBase(config);
  }

  @Test
  public void testGetters() {
    assertNotNull(libraryPlatform.getConfig());
  }

  @Test
  public void testCheckConfig() {
    Set<String> keySet = new HashSet<>();
    keySet.add("key1");
    keySet.add("key3");

    assertTrue(libraryPlatform.checkConfig(keySet));
  }

  @Test(expected = FrameworkRuntimeException.class)
  public void testCheckConfigWithException() {
    Set<String> keySet = new HashSet<>();
    keySet.add("key1");
    keySet.add("key4");

    libraryPlatform.checkConfig(keySet);
  }
}
