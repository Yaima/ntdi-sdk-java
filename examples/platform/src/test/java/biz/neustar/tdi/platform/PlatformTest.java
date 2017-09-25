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

package biz.neustar.tdi.platform;

import static org.junit.Assert.assertNotNull;

import biz.neustar.tdi.fw.platform.facet.crypto.TdiPlatformCryptoShape;
import biz.neustar.tdi.fw.platform.facet.data.TdiPlatformDataShape;
import biz.neustar.tdi.fw.platform.facet.keys.TdiPlatformKeysShape;
import biz.neustar.tdi.fw.platform.facet.time.TdiPlatformTimeShape;
import biz.neustar.tdi.fw.platform.facet.utils.TdiPlatformUtilsShape;
import biz.neustar.tdi.fw.utils.Utils;
import biz.neustar.tdi.platform.facet.TestData;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * JUnit test class for testing Platform class.
 */
public class PlatformTest {

  public static Platform platformObject;

  /**
   * PlatformTest Junit Setup method.
   * 
   * @throws Exception
   * 
   */
  @BeforeClass
  public static void setup() throws Exception {

    String configFilePath = TestData.PLATFORM_CONFIG_BASE_PATH
        + TestData.PLATFORM_CONFIG_FILE_NAME;
    Map<String, Object> config = Utils.jsonFileToMap(new File(configFilePath));

    platformObject = new Platform(config);
  }

  @Test
  public void testGetKeystore() {
    TdiPlatformKeysShape keystore = platformObject.getKeystore();
    assertNotNull(keystore);
  }

  @Test
  public void testGetCrypto() {
    TdiPlatformCryptoShape crypto = platformObject.getCrypto();
    assertNotNull(crypto);
  }

  @Test
  public void testGetDataStore() {
    TdiPlatformDataShape dataStore = platformObject.getDataStore();
    assertNotNull(dataStore);
  }

  @Test
  public void testGetTime() {
    TdiPlatformTimeShape time = platformObject.getTime();
    assertNotNull(time);
  }

  @Test
  public void testgetUtils() {
    TdiPlatformUtilsShape util = platformObject.getUtils();
    assertNotNull(util);
  }

  @Test
  public void testInit() {
    CompletableFuture<Void> future = platformObject.init();
    assertNotNull(future);
  }
}
