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
import biz.neustar.tdi.platform.facet.UtilFacet;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UtilFacetTest {

  static UtilFacet api;

  @BeforeClass
  public static void setup() {
    api = new UtilFacet(TestData.getDummyLibraryPlatform());
  }

  @Test
  public void testEncodeDecod() {
    String originalString = "Test String";
    String encodedStr = api.b64UrlEncode(originalString);
    String decodeStr = api.b64UrlDecode(encodedStr);

    assertEquals(originalString, decodeStr);
  }

  @Test
  public void testGetUuid() {
    String uuid = api.makeUuid();
    assertNotNull(uuid);
  }

  @Test
  public void testRandomFill() {
    byte[] test = new byte[20];
    api.randomFill(test, 20);
  }

  @Test
  public void testGetPlatform() {
    TdiPlatformShape pf = api.getPlatform();
    assertNotNull(pf);
  }

  @Test
  public void testInit() throws InterruptedException, ExecutionException {
    CompletableFuture<Void> future = api.init();
    assertNotNull(future);
    future.get();
  }
  
  @Test
  public void testToBytes() {
    String data = "Test String";
    byte[] dataBytes = api.toBytes(data);
    assertNotNull(dataBytes);
    
    String verify = new String(dataBytes, Charset.forName("UTF-8"));
    assertTrue(verify.equals(data));
  }

  @Test
  public void testToStringData() {
    String testStr = "Test String";
    byte[] testBytes = testStr.getBytes();;
    String data = api.toStringData(testBytes);
    assertNotNull(data);
    assertTrue(data.equals(testStr));
  }
}
