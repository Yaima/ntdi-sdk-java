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

import biz.neustar.tdi.fw.canonicalmessage.TdiClaims;
import biz.neustar.tdi.fw.exception.InvalidFormatException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

  @Test(expected = InvalidFormatException.class)
  public void testInvalidMapToJson() throws Exception {
    Utils.mapToJson(invalidMap);
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

  @Test
  public void testObjectToJson() throws Exception {
    String expectedJson = "{\"prop\":\"Hello\"}";
    assertEquals(expectedJson, Utils.objectToJson(new UnitTestObject()));
  }
  
  @Test(expected = InvalidFormatException.class)
  public void testObjectToJsonWithExp() throws Exception {
    Utils.objectToJson(new Object());
  }
  
  @Test
  public void testJsonToObject() throws Exception {
    String json = "{\"prop\":\"Hello\"}";
    UnitTestObject object = Utils.jsonToObject(json, UnitTestObject.class);
    assertEquals("Hello",object.prop);
  }
  
  @Test(expected = InvalidFormatException.class)
  public void testJsonToObjectWithExp() throws Exception {
    String json = "{\"prop\"\"Hello\"}";
    UnitTestObject object = Utils.jsonToObject(json, UnitTestObject.class);
    assertEquals("Hello",object.prop);
  }
  
  @Test(expected = InvalidFormatException.class)
  public void testJsonToObjectNullJson() throws Exception {
    UnitTestObject object = Utils.jsonToObject(null, UnitTestObject.class);
    assertEquals("Hello",object.prop);
  }

  @Test
  public void testObjectToJsonFileAndViceVersa() throws Exception {
    TdiClaims claims = new TdiClaims();
    claims.iss = "Issuer";
    claims.exp = (long) 1504258110;
    claims.nbf = (long) 1504258092;

    File claimsJsonFile = new File("./testClaimsJsonFile.json");
    Utils.objectToJsonFile(claims, claimsJsonFile);
    TdiClaims readClaims = Utils.jsonFileToObject(claimsJsonFile, TdiClaims.class);

    assertEquals(claims, readClaims);
  }

  @Test(expected = InvalidFormatException.class)
  public void testJsonNullFileToObject() throws Exception {
    Utils.jsonFileToObject(null, TdiClaims.class);
  }

  @Test(expected = InvalidFormatException.class)
  public void testJsonInvalidFileToObject() throws Exception {
    Utils.jsonFileToObject(new File("./InvalidFile"), TdiClaims.class);
  }

  @Test(expected = InvalidFormatException.class)
  public void testJsonEmptyFileToObject() throws Exception {
    Utils.jsonFileToObject(File.createTempFile("emptyFile", "json"), TdiClaims.class);
  }

  @Test(expected = InvalidFormatException.class)
  public void testObjectToJsonNullFile() throws Exception {
    TdiClaims claims = new TdiClaims();
    claims.iss = "Issuer";
    claims.exp = (long) 1504258110;
    claims.nbf = (long) 1504258092;

    Utils.objectToJsonFile(claims, null);
  }
  
  @Test(expected = InvalidFormatException.class)
  public void testObjectToJsonFileWithExp() throws Exception {
    Utils.objectToJsonFile(new Object(), new File("./testClaimsJsonFile.json"));
  }

  @Test
  public void testMapToJsonFile() throws Exception {
    Map<String, Object> map = new HashMap<>();
    map.put("key1", "value1");
    map.put("key2", "value2");
    File claimsJsonFile = File.createTempFile("testClaimsJsonFile", "json");
    Utils.mapToJsonFile(map, claimsJsonFile);
    Map<String, Object> returnedMap  = Utils.jsonFileToMap(claimsJsonFile);

    assertEquals(returnedMap, returnedMap);
  }

  @Test(expected = InvalidFormatException.class)
  public void testInvalidMapToJsonFile() throws Exception {
    Utils.mapToJsonFile(invalidMap, new File("./testClaimsJsonFile.json"));
  }
  
  @Test(expected = InvalidFormatException.class)
  public void testMapToJsonNullFile() throws Exception {
    Map<String, Object> map = new HashMap<>();
    map.put("key1", "value1");
    map.put("key2", "value2");

    Utils.mapToJsonFile(map, null);
  }

  @Test(expected = InvalidFormatException.class)
  public void testJsonNullFileToMap() throws Exception {
    Utils.jsonFileToMap(null);
  }

  @Test(expected = InvalidFormatException.class)
  public void testJsonInvalidFileToMap() throws Exception {
    Utils.jsonFileToMap(new File("./InvalidFile"));
  }
  
  @Test(expected = InvalidFormatException.class)
  public void testJsonEmptyFileToMap() throws Exception {
    Utils.jsonFileToMap(File.createTempFile("emptyFile", "json"));
  }

  public static class UnitTestObject {
    @JsonProperty("prop")
    String prop = "Hello";
  }
  
  public static Map<String, Object> invalidMap = new Map<String, Object>() {

    @Override
    public void clear() {
      
    }

    @Override
    public boolean containsKey(Object key) {
      return false;
    }

    @Override
    public boolean containsValue(Object value) {
      return false;
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
      return null;
    }

    @Override
    public Object get(Object key) {
      return null;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public Set<String> keySet() {
      return null;
    }

    @Override
    public Object put(String key, Object value) {
      return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
      
    }

    @Override
    public Object remove(Object key) {
      return null;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public Collection<Object> values() {
      return null;
    }
  };
}
