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

package biz.neustar.tdi.fw.utils;

import static org.junit.Assert.assertEquals;

import biz.neustar.tdi.fw.exception.InvalidFormatException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test cases for {@link Utils} class.
 */
public class UtilsTest {
  @BeforeClass
  public static void setup() {
    new Utils();
  }
  
  @Test
  public void testMapToJson() throws Exception {
    String expJsonStr = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
    Map<String, String> map = new HashMap<>();
    map.put("key1", "value1");
    map.put("key2", "value2");

    String jsonStr = Utils.mapToJson(map);
    assertEquals(expJsonStr, jsonStr);
  }

  @Test
  public void testJsonToMap() throws Exception {
    String jsonStr = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
    Map<String, Object> expMap = new HashMap<>();
    expMap.put("key1", "value1");
    expMap.put("key2", "value2");

    Map<String, Object> returnMap = Utils.jsonToMap(jsonStr);
    assertEquals(expMap, returnMap);
  }

  @Test(expected = InvalidFormatException.class)
  public void testJsonToMapWithExp() throws Exception {
    String jsonStr = "{\"key1\"\"value1\",\"key2\":\"value2\"}";
    Utils.jsonToMap(jsonStr);
  }
}
