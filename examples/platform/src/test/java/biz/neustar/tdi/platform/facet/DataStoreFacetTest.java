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

package biz.neustar.tdi.platform.facet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.platform.Constants;
import biz.neustar.tdi.platform.facet.DataStoreFacet;
import biz.neustar.tdi.platform.facet.KeystoreFacet;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * JUnit test class for DataStoreFacet class.
 * 
 */
public class DataStoreFacetTest {

  public static KeystoreFacet objKeystoreFacet;
  public static DataStoreFacet dataStoreFacetObj;
  public static TdiPlatformShape pf;
  public static String storeName = "testStoreFile"; // Store name used for
                                                    // testing

  /**
   * DataStoreFacetTest JUnit Setup method.
   * 
   * @throws Exception
   * 
   */
  @BeforeClass
  public static void setup() throws Exception {
    pf = TestData.getDummyLibraryPlatform();
    dataStoreFacetObj = new DataStoreFacet(pf);
  }

  @Test
  public void testGetPlatform() {
    TdiPlatformShape pf = dataStoreFacetObj.getPlatform();
    assertNotNull(pf);
  }

  @Test
  public void testInit() {
    CompletableFuture<Void> future = dataStoreFacetObj.init();
    assertNotNull(future);
  }

  // ----------------------------------------------------------------------------------
  // Test for createStore API
  @Test
  public void testCreateStore() throws InterruptedException, ExecutionException {

    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("key1", "Test value1");
    mapValue.put("key2", 2);

    CompletableFuture<?> future = dataStoreFacetObj.createStore(storeName, mapValue);
    assertNotNull(future);
    future.get();
  }

  @Test(expected = ExecutionException.class)
  public void testCreateStoreInvalidStoreName() throws InterruptedException, ExecutionException {

    String storeName = "!@#$%^&*()_+=--`><.,";

    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("key1", "Test value1");
    mapValue.put("key2", 2);

    CompletableFuture<?> future = dataStoreFacetObj.createStore(storeName, mapValue);
    assertNotNull(future);
    future.get();
  }

  @Test(expected = ExecutionException.class)
  public void testCreateStoreNullName() throws InterruptedException, ExecutionException {

    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("key1", "Test value1");
    mapValue.put("key2", 2);

    CompletableFuture<?> future = dataStoreFacetObj.createStore(null, mapValue);
    assertNotNull(future);
    future.get();
  }

  @Test
  public void testCreateStoreNullValue() throws InterruptedException, ExecutionException {

    Map<String, Object> value = null;
    CompletableFuture<?> future = dataStoreFacetObj.createStore(storeName, value);
    assertNotNull(future);
    future.get();
  }

  @Test
  public void testCreateStoreMultipleValues() throws InterruptedException, ExecutionException {

    // Test data
    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("key1", "Test value1");
    mapValue.put("key2", 2);

    CompletableFuture<?> future = dataStoreFacetObj.createStore(storeName, mapValue);
    assertNotNull(future);
    future.get();
  }

  @Test(expected = ExecutionException.class)
  public void testCreateStoreDirAndFileName() throws InterruptedException, ExecutionException {
    String storeName = "myTest/testDeleteStoreFileInNewDir";
    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("key1", "Value 1");
    CompletableFuture<?> createFuture = dataStoreFacetObj.createStore(storeName, mapValue);
    assertNotNull(createFuture);
    createFuture.get();
  }

  // ----------------------------------------------------------------------------------
  // Test for get API
  @Test
  public void testGet() throws InterruptedException, ExecutionException {

    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("key1", "Test value1");
    mapValue.put("key2", 2);

    CompletableFuture<?> future = dataStoreFacetObj.createStore(storeName, mapValue);
    assertNotNull(future);
    future.get();

    String key = "key1";
    future = dataStoreFacetObj.get(storeName, key);
    assertNotNull(future);

    Object valueObj = future.get();
    assertTrue(((String) mapValue.get(key)).equals(valueObj.toString()));
  }

  @Test
  public void testGetKeyAsStar() throws InterruptedException, ExecutionException {

    String key = "*";
    CompletableFuture<?> future = dataStoreFacetObj.get(storeName, key);
    assertNotNull(future);
    future.get();
  }

  @Test(expected = ExecutionException.class)
  public void testGetInvalidStoreName() throws InterruptedException, ExecutionException {
    String storeName = "";
    String key = "key1";
    CompletableFuture<?> future = dataStoreFacetObj.get(storeName, key);
    assertNotNull(future);
    future.get();
  }

  @Test(expected = ExecutionException.class)
  public void testGetInvalidKey() throws InterruptedException, ExecutionException {
    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("key1", "Test value2");
    mapValue.put("key2", 3);

    CompletableFuture<?> createFuture = dataStoreFacetObj.createStore(storeName, mapValue);
    assertNotNull(createFuture);
    createFuture.get();

    String key = "";
    CompletableFuture<?> getFuture = dataStoreFacetObj.get(storeName, key);
    assertNotNull(getFuture);
    getFuture.get();
  }

  // ----------------------------------------------------------------------------------
  // Test for set API
  @Test
  public void testSet() throws InterruptedException, ExecutionException {

    String key = "NewKey1";
    Object value = "Test New 1";
    CompletableFuture<Void> future = dataStoreFacetObj.set(storeName, key, value);
    assertNotNull(future);
    future.get();

    CompletableFuture<?> getFuture = dataStoreFacetObj.get(storeName, key);
    assertNotNull(getFuture);
    String getValue = (String) getFuture.get(); // Convert to String as we know
                                                // the type
    assertTrue(getValue.equalsIgnoreCase((String) value));
  }

  @Test
  public void testSetNewStoreName() throws InterruptedException, ExecutionException {

    String storeName = "testSetStoreWithNewName";
    String key = "NewKey1";
    Object value = "Test New 1";
    CompletableFuture<Void> future = dataStoreFacetObj.set(storeName, key, value);
    assertNotNull(future);
    future.get();

    CompletableFuture<?> getFuture = dataStoreFacetObj.get(storeName, key);
    assertNotNull(getFuture);
    String getValue = (String) getFuture.get(); // Convert to String as we know
                                                // the type
    assertTrue(getValue.equalsIgnoreCase((String) value));
  }

  @Test(expected = ExecutionException.class)
  public void testSetInvalidKey() throws InterruptedException, ExecutionException {
    String storeName = "!@#$%^&**()_+=-.,<>:;'";
    String key = "NewKey1";
    Object value = "Test New 1";
    CompletableFuture<Void> future = dataStoreFacetObj.set(storeName, key, value);
    assertNotNull(future);
    future.get();
  }

  @Test(expected = ExecutionException.class)
  public void testSetEmptyStore() throws InterruptedException, ExecutionException {
    String storeName = "";
    String key = "NewKey1";
    Object value = "Test New 1";
    CompletableFuture<Void> future = dataStoreFacetObj.set(storeName, key, value);
    assertNotNull(future);
    future.get();
  }

  @Test(expected = ExecutionException.class)
  public void testSetStarKey() throws InterruptedException, ExecutionException {
    String key = "*";
    Object value = "Test New 1";
    CompletableFuture<Void> future = dataStoreFacetObj.set(storeName, key, value);
    assertNotNull(future);
    future.get();
  }

  // ----------------------------------------------------------------------------------
  // Test for kesy API
  @Test
  public void testKeys() throws InterruptedException, ExecutionException {

    String storeName = "testGetKeysFile";
    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("key1", "Value 1");
    mapValue.put("key2", "Value 2");
    mapValue.put("key3", "Value 3");

    CompletableFuture<?> createFuture = dataStoreFacetObj.createStore(storeName, mapValue);
    assertNotNull(createFuture);
    createFuture.get();

    CompletableFuture<List<String>> keysFuture = dataStoreFacetObj.keys(storeName);
    assertNotNull(keysFuture);
    List<String> list = keysFuture.get();
    assertTrue(list.size() == mapValue.size());

    for (String key : list) {
      assertTrue(mapValue.containsKey(key));
    }
  }

  @Test(expected = ExecutionException.class)
  public void testKeysInvalidStorename() throws InterruptedException, ExecutionException {

    String storeName = "";
    CompletableFuture<List<String>> keysFuture = dataStoreFacetObj.keys(storeName);
    assertNotNull(keysFuture);
    keysFuture.get();
  }

  // ----------------------------------------------------------------------------------
  // Test for kesy API
  @Test(expected = ExecutionException.class)
  public void testDrop() throws InterruptedException, ExecutionException {
    String storeName = "testDropKeyTestFile";
    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("key1", "Value 1");
    mapValue.put("key2", "Value 2");
    mapValue.put("key3", "Value 3");

    CompletableFuture<?> createFuture = dataStoreFacetObj.createStore(storeName, mapValue);
    assertNotNull(createFuture);
    createFuture.get();

    String key = "key1";
    CompletableFuture<Void> dropFuture = dataStoreFacetObj.drop(storeName, key);
    assertNotNull(dropFuture);
    dropFuture.get();

    CompletableFuture<?> getFuture = dataStoreFacetObj.get(storeName, key);
    assertNotNull(getFuture);
    getFuture.get();
  }

  @Test(expected = ExecutionException.class)
  public void testDropInvalidKey() throws InterruptedException, ExecutionException {
    String storeName = "testDropStoreFile";
    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("key1", "Value 1");
    mapValue.put("key2", "Value 2");
    mapValue.put("key3", "Value 3");

    CompletableFuture<?> createFuture = dataStoreFacetObj.createStore(storeName, mapValue);
    assertNotNull(createFuture);
    createFuture.get();

    String key = "";
    CompletableFuture<Void> dropFuture = dataStoreFacetObj.drop(storeName, key);
    assertNotNull(dropFuture);
    dropFuture.get();
  }

  @Test(expected = ExecutionException.class)
  public void testDropInvalidStoreName() throws InterruptedException, ExecutionException {

    String storeName = "testDropStoreFile";
    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("key1", "Value 1");
    mapValue.put("key2", "Value 2");
    mapValue.put("key3", "Value 3");

    CompletableFuture<?> createFuture = dataStoreFacetObj.createStore(storeName, mapValue);
    assertNotNull(createFuture);
    createFuture.get();

    storeName = "";
    String key = "key1";
    CompletableFuture<Void> dropFuture = dataStoreFacetObj.drop(storeName, key);
    assertNotNull(dropFuture);
    dropFuture.get();
  }

  // ----------------------------------------------------------------------------------
  // Test for deleteStore API
  @Test
  public void testDeleteStore() throws InterruptedException, ExecutionException {
    String storeName = "testDeleteStoreFile";
    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("key1", "Value 1");
    CompletableFuture<?> createFuture = dataStoreFacetObj.createStore(storeName, mapValue);
    assertNotNull(createFuture);
    createFuture.get();

    CompletableFuture<Void> deleteFuture = dataStoreFacetObj.deleteStore(storeName);
    assertNotNull(deleteFuture);
    deleteFuture.get();
  }

  @Test(expected = ExecutionException.class)
  public void testDeleteStoreInvalidStore() throws InterruptedException, ExecutionException {
    String storeName = "";
    CompletableFuture<Void> deleteFuture = dataStoreFacetObj.deleteStore(storeName);
    assertNotNull(deleteFuture);
    deleteFuture.get();
  }

  @Test(expected = ExecutionException.class)
  public void testDeleteStoreInvalidFile() throws InterruptedException, ExecutionException {
    String storeName = "testDeleteStoreFileNew";
    Map<String, Object> mapValue = new HashMap<>();
    mapValue.put("key1", "Value 1");
    CompletableFuture<?> createFuture = dataStoreFacetObj.createStore(storeName, mapValue);
    assertNotNull(createFuture);
    createFuture.get();

    Map<String, Object> configMap = pf.getConfig();

    @SuppressWarnings("unchecked")
    Map<String, Object> basePathConfig = (Map<String, Object>) configMap
        .get(Constants.PLATFORM_CONFIG_KEY_DATASTORE);
    String storeBasePath = (String) basePathConfig.get(Constants.PLATFORM_CONFIG_KEY_BASEPATH);
    System.out
        .println("DataStoreFacet: testDeleteStoreInvalidFile: storeBasePath:" + storeBasePath);
    if (StringUtils.isEmpty(storeBasePath)) {
      storeBasePath = Constants.PLATFORM_DEFAULT_BASE_PATH;
    }

    // Delete data file
    String filePath = storeBasePath + storeName + ".dat";
    File file = new File(filePath);
    file.delete();

    CompletableFuture<Void> deleteFuture = dataStoreFacetObj.deleteStore(storeName);
    assertNotNull(deleteFuture);
    deleteFuture.get();
  }

  @Test
  public void testEmptyConfig() throws Exception {
    TdiPlatformShape testPlatform = TestData.getDummyLibraryPlatform();
    @SuppressWarnings("unchecked")
    Map<String, Object> datastoreConfig = (Map<String, Object>) testPlatform.getConfig()
        .get(Constants.PLATFORM_CONFIG_KEY_DATASTORE);

    datastoreConfig.remove(Constants.PLATFORM_CONFIG_KEY_BASEPATH);
    DataStoreFacet testStoreFacet = new DataStoreFacet(testPlatform);
    
    assertEquals(Constants.PLATFORM_DEFAULT_BASE_PATH, testStoreFacet.storeBasePath);
    
    testPlatform.getConfig().remove(Constants.PLATFORM_CONFIG_KEY_DATASTORE);
    testStoreFacet = new DataStoreFacet(testPlatform);
    assertEquals(Constants.PLATFORM_DEFAULT_BASE_PATH, testStoreFacet.storeBasePath);
  }
}
